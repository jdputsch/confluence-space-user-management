package csum.confluence.permissionmgmt.service.impl;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.persistence.dao.SpaceDao;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.Group;
import com.atlassian.user.search.page.DefaultPager;
import com.atlassian.user.search.page.Pager;
import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.service.GroupManagementService;
import csum.confluence.permissionmgmt.service.exception.FindException;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.group.GroupNameUtil;
import csum.confluence.permissionmgmt.util.group.GroupUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 6, 2007
 * Time: 11:37:43 AM
 */
public abstract class BaseGroupManagementService implements GroupManagementService {

    protected Log log = LogFactory.getLog(this.getClass());

    // assuming these are autowired
    private BootstrapManager bootstrapManager;
    private BandanaManager bandanaManager;
    private SpaceDao spaceDao;
    private SpacePermissionManager spacePermissionManager;
    protected UserAccessor userAccessor;
    private CustomPermissionConfiguration customPermissionConfiguration;

    public BaseGroupManagementService() {
        log.debug("ConfluenceGroupManagementService start constructor");
        bootstrapManager = (BootstrapManager) ContainerManager.getComponent("bootstrapManager");
        bandanaManager = (BandanaManager) ContainerManager.getComponent("bandanaManager");
        spaceDao = (SpaceDao) ContainerManager.getComponent("spaceDao");
        spacePermissionManager = (SpacePermissionManager) ContainerManager.getComponent("spacePermissionManager");
        userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
        //customPermissionConfiguration = (CustomPermissionConfiguration) ConfluenceUtil.loadComponentWithRetry("customPermissionConfiguration");
        log.debug("ConfluenceGroupManagementService end cosntructor");
    }

    public Pager findGroups(ServiceContext context) throws FindException {
        log.debug("findGroups() called");
        Map mapWithGroupnamesAsKeys = getGroupsWithViewspacePermissionAsKeysAsMapWithGroupnamesAsKeys(context);
        List groups = getGroupsThatMatchNamePatternExcludingConfluenceAdministrators(mapWithGroupnamesAsKeys, context);
        GroupUtil.sortGroupsByGroupnameAscending(groups);
        Pager pager = new DefaultPager(groups);
        return pager;
    }

    public boolean isAllowedToManageGroup( ServiceContext context, String groupName ) throws FindException {
        log.debug("isAllowedToManageGroup() called. groupName=" + groupName);
        Map mapWithGroupnamesAsKeys = getGroupsWithViewspacePermissionAsKeysAsMapWithGroupnamesAsKeys(context);
        List groupNames = getGroupnamesThatMatchNamePatternExcludingConfluenceAdministrators(mapWithGroupnamesAsKeys, context);
        return groupNames.contains(groupName);         
    }

    private List getGroupsThatMatchNamePatternExcludingConfluenceAdministrators(Map mapWithGroupnamesAsKeys, ServiceContext context) {
        log.debug("getGroupsThatMatchNamePatternExcludingConfluenceAdministrators() called");
        List groupNames = getGroupnamesThatMatchNamePatternExcludingConfluenceAdministrators(mapWithGroupnamesAsKeys, context);
        List groups = new ArrayList();
        for (int i=0; i<groupNames.size(); i++) {
            String groupName = (String)groupNames.get(i);
            Group group = userAccessor.getGroup(groupName);
            groups.add(group);
        }
        return groups;
    }

    private List getGroupnamesThatMatchNamePatternExcludingConfluenceAdministrators(Map mapWithGroupnamesAsKeys, ServiceContext context) {
        log.debug("getGroupsThatMatchNamePatternExcludingConfluenceAdministrators() called");
        List groupNames = new ArrayList();

        ArrayList notAllowedUserGroups = new ArrayList();
        notAllowedUserGroups.add("confluence-administrators");

        CustomPermissionConfiguration config = getCustomPermissionConfiguration();
        String spaceKey = context.getSpace().getKey();
        String prefix = GroupNameUtil.replaceSpaceKey(config.getNewGroupNameCreationPrefixPattern(), spaceKey);
        String suffix = GroupNameUtil.replaceSpaceKey(config.getNewGroupNameCreationSuffixPattern(), spaceKey);

        for (Iterator iterator = mapWithGroupnamesAsKeys.keySet().iterator(); iterator.hasNext();) {
            String groupName = (String) iterator.next();
            //If notAllowedUserGroups doesn't contain this group name
            //and group name matches the pattern, then only add this user-group for display.
            //log.debug("Selected Groups .....");
            boolean isPatternMatch = GroupNameUtil.doesGroupMatchPattern(groupName, prefix, suffix);
            if ((!notAllowedUserGroups.contains(groupName)) && isPatternMatch) {
                //log.debug("Group '" + grpName + "' allowed and matched pattern " + pat.pattern() );
                groupNames.add(groupName);
            } else {
                //log.debug("Group '" + grpName + "' not allowed or didn't match pattern. notAllowedUserGroups=" + StringUtil.convertCollectionToCommaDelimitedString(notAllowedUserGroups) + " isPatternMatch=" + isPatternMatch + " pattern=" + pat.pattern());
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

    public BandanaManager getBandanaManager() {
        return bandanaManager;
    }

    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }

    public BootstrapManager getBootstrapManager() {
        return bootstrapManager;
    }

    public void setBootstrapManager(BootstrapManager bootstrapManager) {
        this.bootstrapManager = bootstrapManager;
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }

    public SpacePermissionManager getSpacePermissionManager() {
        return spacePermissionManager;
    }

    public void setSpacePermissionManager(SpacePermissionManager spacePermissionManager) {
        this.spacePermissionManager = spacePermissionManager;
    }

    public SpaceDao getSpaceDao() {
        return spaceDao;
    }

    public void setSpaceDao(SpaceDao spaceDao) {
        this.spaceDao = spaceDao;
    }

    public UserAccessor getUserAccessor() {
        return userAccessor;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }
}
