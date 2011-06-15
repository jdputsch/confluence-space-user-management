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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UserAndGroupManagementService {

    // What EmbeddedCrowdUserManager uses...
    private static final int RANDOM_PASSWORD_LENGTH = 22;

    protected Log log = LogFactory.getLog(this.getClass());

    protected CrowdService crowdService;
    protected CrowdDirectoryService crowdDirectoryService;
    protected GroupManager groupManager;
    protected UserAccessor userAccessor;

    @Autowired
    public UserAndGroupManagementService(CrowdService crowdService,
                                         CrowdDirectoryService crowdDirectoryService,
                                         GroupManager groupManager,
                                         UserAccessor userAccessor) {
        this.crowdDirectoryService = crowdDirectoryService;
        this.crowdService = crowdService;
        this.groupManager = groupManager;
        this.userAccessor = userAccessor;

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

    public boolean isReadOnly(Group group) {
        if (group == null) {
            log.warn("Attempted to check isReadOnly on null group. Returning false.");
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

    public Group getGroup(String groupName) {
        Group group = null;
        try {
            group = groupManager.getGroup(groupName);
        } catch (Throwable t) {
            log.error("Problem getting group '" + groupName + "'", t);
        }
        return group;
    }

    public Group addGroup(String groupName) {
        Group group = null;
        try {
            group = groupManager.createGroup(groupName);
        } catch (Throwable t) {
            log.error("Problem creating group '" + groupName + "'", t);
        }
        return group;
    }

    public void removeGroup(Group group) {
        if (group == null) {
            log.warn("Attempted to delete null group. Ignoring.");
        } else {
            try {
                groupManager.removeGroup(group);
            } catch (Throwable t) {
                log.error("Problem removing group with name '" + group.getName() + "'", t);
            }
        }
    }

    public Pager getMemberNames(Group group) {
        Pager pager = null;
        if (group == null) {
            log.warn("Attempted to get members of null group. Ignoring.");
        } else {
            try {
                pager = groupManager.getMemberNames(group);
            } catch (Throwable t) {
                log.error("Problem getting members of group '" + group.getName() + "'", t);
            }
        }
        return pager;
    }

    public void addMembership(Group group, User user) {
        if (group == null) {
            log.warn("Attempted to add user to null group. Ignoring.");
        } else if (user == null) {
            log.warn("Attempted to add null user to group. Ignoring.");
        } else {
            try {
                groupManager.addMembership(group, user);
            } catch (Throwable t) {
                log.error("Problem adding user '" + user.getName() + "' to group '" + group.getName() + "'", t);
            }
        }
    }

    public void removeMembership(Group group, User user) {
        if (group == null) {
            log.warn("Attempted to remove user from null group. Ignoring.");
        } else if (user == null) {
            log.warn("Attempted to remove null user from group. Ignoring.");
        } else {
            try {
                groupManager.removeMembership(group, user);
            } catch (Throwable t) {
                log.error("Problem removing user '" + user.getName() + "' from group '" + group.getName() + "'", t);
            }
        }
    }
}
