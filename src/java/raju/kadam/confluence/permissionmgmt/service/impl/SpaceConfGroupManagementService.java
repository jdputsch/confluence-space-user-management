package raju.kadam.confluence.permissionmgmt.service.impl;

import raju.kadam.confluence.permissionmgmt.service.GroupManagementService;
import raju.kadam.confluence.permissionmgmt.util.GroupMatchingUtil;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.util.StringUtil;
import raju.kadam.util.ListUtil;

import java.util.*;
import java.util.regex.Pattern;

import com.atlassian.user.Group;
import com.atlassian.confluence.spaces.actions.SpaceAdministrative;
import com.atlassian.confluence.spaces.persistence.dao.SpaceDao;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.bandana.BandanaManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:47:08 AM
 */
public class SpaceConfGroupManagementService implements GroupManagementService {

    private Log log = LogFactory.getLog(this.getClass());

    // assuming these are autowired
    private BootstrapManager bootStrapManager;
    private BandanaManager bandanaManager;
    private SpaceDao spDao;
    private SpacePermissionManager spacePermissionManager;
    private UserAccessor userAccessor;
    private CustomPermissionConfiguration customPermissionConfiguration;

    public List findGroups(Space space) {

        ArrayList notAllowedUserGroups = new ArrayList();
    	notAllowedUserGroups.add("confluence-administrators");

    	Pattern pat = GroupMatchingUtil.createGroupMatchingPattern(getBandanaManager(), getCustomPermissionConfiguration().getUserGroupsMatchingPattern());

        List result = new ArrayList();

        if (space==null) {
            log.warn("Space is null! Will not be able to get groups");
            return result;
        }

        //VIEWSPACE_PERMISSION is basic permission that every user group can have.
        Map map = spacePermissionManager.getGroupsForPermissionType(SpacePermission.VIEWSPACE_PERMISSION, space);
        if ( map==null || map.size()==0 ) {
            log.debug("No groups with permissiontype SpacePermission.VIEWSPACE_PERMISSION");
        }
        else {
            log.debug("Got the following groups with permissiontype SpacePermission.VIEWSPACE_PERMISSION: " + StringUtil.convertCollectionToCommaDelimitedString(map.keySet()));
        }

        for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();)
        {
        	String grpName = (String) iterator.next();
            //If notAllowedUserGroups doesn't contain this group name
        	//and group name matches the pattern, then only add this user-group for display.
    		//log.debug("Selected Groups .....");
            boolean isPatternMatch = GroupMatchingUtil.doesGroupMatchPattern(grpName, pat);
            if( (!notAllowedUserGroups.contains(grpName)) && isPatternMatch)
        	{
        		log.debug("group '" + grpName + "' allowed");
        		result.add(userAccessor.getGroup(grpName));
            }
            else {
                log.debug("group '" + grpName + "' not allowed. notAllowedUserGroups=" + StringUtil.convertCollectionToCommaDelimitedString(notAllowedUserGroups) + " isPatternMatch=" + isPatternMatch );
            }
            //log.debug("-------End of Groups---------");

        }

        Collections.sort(result, new Comparator()
        {
            public int compare(Object o, Object o1)
            {
                return ((Group) o).getName().compareToIgnoreCase(((Group) o1).getName());
            }
        });

        return result;
    }

    public void createGroup(Group group, Space space) {

    }

    public void deleteGroup(Group group, Space space) {

    }


    public BandanaManager getBandanaManager() {
        return bandanaManager;
    }

    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }

    public BootstrapManager getBootStrapManager() {
        return bootStrapManager;
    }

    public void setBootStrapManager(BootstrapManager bootStrapManager) {
        this.bootStrapManager = bootStrapManager;
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

    public SpaceDao getSpDao() {
        return spDao;
    }

    public void setSpDao(SpaceDao spDao) {
        this.spDao = spDao;
    }
    
    public UserAccessor getUserAccessor() {
        return userAccessor;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }
}
