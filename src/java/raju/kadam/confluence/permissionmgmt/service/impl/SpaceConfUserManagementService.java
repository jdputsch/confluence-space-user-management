package raju.kadam.confluence.permissionmgmt.service.impl;

import raju.kadam.confluence.permissionmgmt.service.UserManagementService;

import java.util.List;

import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.confluence.user.UserAccessor;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:48:36 AM
 */
public class SpaceConfUserManagementService implements UserManagementService {

    private UserAccessor userAccessor;

    public SpaceConfUserManagementService() {
        super();

        // autowiring will set the components on this class as needed
        com.atlassian.spring.container.ContainerManager.autowireComponent(this);
    }

    public List findUsersForGroup(Group group) {
        Pager pager = userAccessor.getMemberNames(group);
        return PagerUtils.toList(pager);
    }

    public List addUserToGroup(User user, Group group) {
        return null;
    }

    public List removeUserFromGroup(User user, Group group) {
        return null;
    }


    public UserAccessor getUserAccessor() {
        return userAccessor;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }
}
