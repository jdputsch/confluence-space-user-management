package raju.kadam.confluence.permissionmgmt.service.impl;

import raju.kadam.confluence.permissionmgmt.service.UserManagementService;

import java.util.List;

import com.atlassian.user.Group;
import com.atlassian.user.User;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:48:36 AM
 */
public class SpaceConfUserManagementService implements UserManagementService {

    public SpaceConfUserManagementService() {
        super();

        // autowiring will set the components on this class as needed
        com.atlassian.spring.container.ContainerManager.autowireComponent(this);
    }

    public List findUsersForGroup(Group group) {
        return null;
    }

    public List addUserToGroup(User user, Group group) {
        return null;
    }

    public List removeUserFromGroup(User user, Group group) {
        return null;
    }
}
