package raju.kadam.confluence.permissionmgmt.service;

import com.atlassian.user.Group;
import com.atlassian.user.User;

import java.util.List;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:37:32 AM
 */
public interface UserManagementService {

    public List findUsersForGroup( Group group );

    public List addUserToGroup( User user, Group group );

    public List removeUserFromGroup( User user, Group group );
}
