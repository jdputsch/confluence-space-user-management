package csum.confluence.permissionmgmt.service.impl;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.util.SecureRandomStringUtils;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;
import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.security.password.Credential;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import com.atlassian.user.UserManager;
import com.atlassian.user.search.page.DefaultPager;
import com.atlassian.user.search.page.Pager;
import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.service.GroupManagementService;
import csum.confluence.permissionmgmt.service.exception.FindException;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.group.GroupNameUtil;
import csum.confluence.permissionmgmt.util.group.GroupUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;

public class UserAndGroupManagementService {

    // What EmbeddedCrowdUserManager uses...
    private static final int RANDOM_PASSWORD_LENGTH = 22;

    protected Log log = LogFactory.getLog(this.getClass());

    protected CrowdService crowdService;
    protected CrowdDirectoryService crowdDirectoryService;
    protected GroupManager groupManager;
    protected UserAccessor userAccessor;
    // assuming these are autowired
    protected SpacePermissionManager spacePermissionManager;
    protected CustomPermissionConfiguration customPermissionConfiguration;


    @Autowired
    public UserAndGroupManagementService(SpacePermissionManager spacePermissionManager,
                                         CrowdService crowdService,
                                         CustomPermissionConfiguration customPermissionConfiguration,
                                         GroupManager groupManager,
                                         CrowdDirectoryService crowdDirectoryService,
                                         UserAccessor userAccessor) {
        this.crowdDirectoryService = crowdDirectoryService;
        this.crowdService = crowdService;
        this.groupManager = groupManager;
        this.userAccessor = userAccessor;
        this.spacePermissionManager = spacePermissionManager;
        this.customPermissionConfiguration = customPermissionConfiguration;

        if (crowdService==null) {
			throw new RuntimeException("crowdService was not autowired in UserAndGroupManagementService");
        }
        else if (crowdDirectoryService==null) {
			throw new RuntimeException("crowdDirectoryService was not autowired in UserAndGroupManagementService");
        }
        else if (groupManager==null) {
			throw new RuntimeException("groupManager was not autowired in UserAndGroupManagementService");
        }
        else if (userAccessor==null) {
            throw new RuntimeException("userAccessor was not autowired in UserAndGroupManagementService");
        }
        else if (spacePermissionManager==null) {
            throw new RuntimeException("spacePermissionManager was not autowired in UserAndGroupManagementService");
        }
        else if (customPermissionConfiguration==null) {
			throw new RuntimeException("customPermissionConfiguration was not autowired in UserAndGroupManagementService");
        }
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public User getUser(String username) {
        User user = null;
        try {
            com.atlassian.crowd.embedded.api.User crowdUser = crowdService.getUser(username);
            user = userAccessor.getUser(crowdUser.getName());
        } catch (Throwable t) {
            log.error("Problem getting user '" + username + "'", t);
        }

        return user;
    }

    public boolean isReadOnly(ServiceContext context, Group group) {
        if (group == null) {
            log.warn("Attempted to check isReadOnly on null group. Returning false.");
            return true;
        } else if (group.getName() == null) {
            log.warn("Attempted to check isReadOnly on null group name. Returning false.");
            return true;
        } else if (!isAllowedToManageGroup(context, group.getName())) {
            log.warn("Attempted to check read only status of group without permission. Returning true.");
            return true;
        }

        boolean result = false;
        try {
            result = groupManager.isReadOnly(group);
        } catch (Throwable t) {
            log.error("Problem checking isReadOnly status of group '" + group.getName() + "'. Will assume isn't read-only.", t);
        }

        return result;
    }

    // TODO: define directoryId via dropdown in plugin config?
    private Long findFirstWritableDirectoryId() {
        List<Directory> directories = crowdDirectoryService.findAllDirectories();
        if (log.isDebugEnabled()) {
            log.debug("Attempting to find a crowd directory that allows user creation. Found " + directories.size() + " directories.");
        }

        for (int i=0; i<directories.size(); i++) {
            Directory directory = directories.get(i);
            if (directory!=null) {
                boolean writable = false;
                Set allowedOperations = directory.getAllowedOperations();
                Iterator iter = allowedOperations.iterator();
                while(iter.hasNext()) {
                    OperationType operationType = (OperationType)iter.next();
                    if (operationType == OperationType.CREATE_USER) {
                        writable = true;
                        if (log.isDebugEnabled()) {
                            log.debug("Directory with id " + directory.getId() + " and name " + directory.getName() + " allows user creation.");
                        }
                        return directory.getId();
                    }
                    else {
                        if (log.isDebugEnabled()) {
                            log.debug("Directory with id " + directory.getId() + " and name " + directory.getName() + " does not allow user creation.");
                        }
                    }
                }
            }
        }

        log.warn("No crowd directory available currently that allows user creation. Please turn off the ability to create users in the Confluence Space User Management plugin, because there is no way to create users.");

        return null;
    }

    public User addUser(String userName, String email, String fullName) {
        User user = null;
        try {
            UserTemplate template = new UserTemplate(userName);
            template.setDisplayName(fullName);
            template.setEmailAddress(email);
            template.setActive(true);
            // Crowd must have a password. Here we use the same method that EmbeddedCrowdUserManager uses in Confluence 3.5.x.
            com.atlassian.crowd.embedded.api.User cUser = crowdService.addUser(template, Credential.unencrypted(SecureRandomStringUtils.getInstance().randomAlphanumericString(RANDOM_PASSWORD_LENGTH)).getValue());
            if (cUser == null) {
                log.warn("crowdService.addUser for " + userName + " returned null.");
            }
            else {
                // could use Conversions.TO_ATLASSIAN_USER.apply(crowdUser)? Is class accessible?
                user = userAccessor.getUser(userName);
                if (user == null) {
                    log.warn("userAccessor.getUser(" + userName + ") returned null! Did user creation fail or is it too slow to get it right away?");
                }
            }
        } catch (Throwable t) {
            log.error("Problem creating user '" + userName + "'", t);
        }
        return user;
    }

    public Group getGroup(ServiceContext context, String groupName) {
        if (!isAllowedToManageGroup(context, groupName)) {
            log.warn("Attempted to get group without permission. Ignoring.");
            return null;
        }
      
        Group group = null;
        try {
            group = groupManager.getGroup(groupName);
        } catch (Throwable t) {
            log.error("Problem getting group '" + groupName + "'", t);
        }
        return group;
    }

    public Group addGroup(ServiceContext context, String groupName) {
        if (!isAllowedToManageGroup(context, groupName)) {
            log.warn("Attempted to add group without permission. Ignoring.");
            return null;
        }
      
        Group group = null;
        try {
            group = groupManager.createGroup(groupName);
        } catch (Throwable t) {
            log.error("Problem creating group '" + groupName + "'", t);
        }
        return group;
    }

    public void removeGroup(ServiceContext context, Group group) {
        if (group == null) {
            log.warn("Attempted to delete null group. Ignoring.");
        } else if (!isAllowedToManageGroup(context, group.getName())) {
            log.warn("Attempted to remove group without permission. Ignoring.");
        } else {
            try {
                groupManager.removeGroup(group);
            } catch (Throwable t) {
                log.error("Problem removing group with name '" + group.getName() + "'", t);
            }
        }
    }

    public Pager getMemberNames(ServiceContext context, Group group) {
        Pager pager = null;
        if (group == null) {
            log.warn("Attempted to get members of null group. Ignoring.");
        } else if (!isAllowedToManageGroup(context, group.getName())) {
            log.warn("Attempted to get members of group without permission. Ignoring.");
        } else {
            try {
                pager = groupManager.getMemberNames(group);
            } catch (Throwable t) {
                log.error("Problem getting members of group '" + group.getName() + "'", t);
            }
        }
        return pager;
    }

    public void addMembership(ServiceContext context, Group group, User user) {
        if (group == null) {
            log.warn("Attempted to add user to null group. Ignoring.");
        } else if (user == null) {
            log.warn("Attempted to add null user to group. Ignoring.");
        } else if (!isAllowedToManageGroup(context, group.getName())) {
            log.warn("Attempted to add membership without permission. Ignoring.");
        } else {
            try {
                groupManager.addMembership(group, user);
            } catch (Throwable t) {
                log.error("Problem adding user '" + user.getName() + "' to group '" + group.getName() + "'", t);
            }
        }
    }

    public void removeMembership(ServiceContext context, Group group, User user) {
        if (group == null) {
            log.warn("Attempted to remove user from null group. Ignoring.");
        } else if (user == null) {
            log.warn("Attempted to remove null user from group. Ignoring.");
        } else if (!isAllowedToManageGroup(context, group.getName())) {
            log.warn("Attempted to remove membership without permission. Ignoring.");
        } else {
            try {
                groupManager.removeMembership(group, user);
            } catch (Throwable t) {
                log.error("Problem removing user '" + user.getName() + "' from group '" + group.getName() + "'", t);
            }
        }
    }
    
    public boolean isAllowedToManageGroup(ServiceContext context, String groupName) {
        log.debug("isAllowedToManageGroup() called. groupName=" + groupName);
        Map mapWithGroupnamesAsKeys = getGroupsWithViewspacePermissionAsKeysAsMapWithGroupnamesAsKeys(context);
        List groupNames = getReadWriteGroupnamesThatMatchNamePatternExcludingConfluenceAdministrators(mapWithGroupnamesAsKeys, context);
        return groupNames.contains(groupName);
    }

    private List getReadWriteGroupsThatMatchNamePatternExcludingConfluenceAdministrators(Map mapWithGroupnamesAsKeys, ServiceContext context) {
        log.debug("getGroupsThatMatchNamePatternExcludingConfluenceAdministrators() called");
        List groupNames = getReadWriteGroupnamesThatMatchNamePatternExcludingConfluenceAdministrators(mapWithGroupnamesAsKeys, context);
        List groups = new ArrayList();
        for (int i = 0; i < groupNames.size(); i++) {
            String groupName = (String) groupNames.get(i);
            Group group = getGroup(context, groupName);
            if (isReadOnly(context, group)) {
                log.debug("group '" + groupName + "' is read-only according to Confluence, therefore it cannot be managed by CSUM.");
            } else {
                groups.add(group);
            }
        }
        return groups;
    }

    private List getReadWriteGroupnamesThatMatchNamePatternExcludingConfluenceAdministrators(Map mapWithGroupnamesAsKeys, ServiceContext context) {
        log.debug("getGroupsThatMatchNamePatternExcludingConfluenceAdministrators() called");
        List groupNames = new ArrayList();

        ArrayList notAllowedUser = new ArrayList();
        notAllowedUser.add("confluence-administrators");

        CustomPermissionConfiguration config = getCustomPermissionConfiguration();
        String spaceKey = context.getSpace().getKey();
        String prefix = GroupNameUtil.replaceSpaceKey(config.getNewGroupNameCreationPrefixPattern(), spaceKey);
        String suffix = GroupNameUtil.replaceSpaceKey(config.getNewGroupNameCreationSuffixPattern(), spaceKey);

        for (Iterator iterator = mapWithGroupnamesAsKeys.keySet().iterator(); iterator.hasNext();) {
            String groupName = (String) iterator.next();
            //If notAllowedUser doesn't contain this group name
            //and group name matches the pattern, then only add this user-group for display.
            //log.debug("Selected Groups .....");
            boolean isPatternMatch = GroupNameUtil.doesGroupMatchPattern(groupName, prefix, suffix);
            if ((!notAllowedUser.contains(groupName)) && isPatternMatch) {
                //log.debug("Group '" + grpName + "' allowed and matched pattern " + pat.pattern() );
                groupNames.add(groupName);
            } else {
                //log.debug("Group '" + grpName + "' not allowed or didn't match pattern. notAllowedUser=" + StringUtil.convertCollectionToCommaDelimitedString(notAllowedUser) + " isPatternMatch=" + isPatternMatch + " pattern=" + pat.pattern());
            }
            //log.debug("-------End of Groups---------");

        }
        return groupNames;
    }

    private Map getGroupsWithViewspacePermissionAsKeysAsMapWithGroupnamesAsKeys(ServiceContext context) {
        log.debug("getGroupsWithViewspacePermissionAsKeysAsMapWithGroupnamesAsKeys() called");
        Space space = context.getSpace();
        //VIEWSPACE_PERMISSION is basic permission that every user group can have.
        Map map = spacePermissionManager.getGroupsForPermissionType(SpacePermission.VIEWSPACE_PERMISSION, space);
        if (map == null || map.size() == 0) {
            log.debug("No groups with permissiontype SpacePermission.VIEWSPACE_PERMISSION");
        } else {
            log.debug("Got the following groups with permissiontype SpacePermission.VIEWSPACE_PERMISSION: " + StringUtil.convertCollectionToCommaDelimitedString(map.keySet()));
        }
        return map;
    }

    public Pager findGroups(ServiceContext context) throws FindException {
        log.debug("findGroups() called");
        Map mapWithGroupnamesAsKeys = getGroupsWithViewspacePermissionAsKeysAsMapWithGroupnamesAsKeys(context);
        List groups = getReadWriteGroupsThatMatchNamePatternExcludingConfluenceAdministrators(mapWithGroupnamesAsKeys, context);
        GroupUtil.sortGroupsByGroupnameAscending(groups);
        Pager pager = new DefaultPager(groups);
        return pager;
    }
}
