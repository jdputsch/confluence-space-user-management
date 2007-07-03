package raju.kadam.confluence.permissionmgmt.service;

import com.atlassian.user.Group;
import com.atlassian.user.User;

import java.util.List;

import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:37:32 AM
 */
public interface UserManagementService {

    public AdvancedUserQueryResults findUsers( AdvancedUserQuery advancedUserQuery, ServiceContext context ) throws FindException;

    public List findUsersForGroup( String groupName, ServiceContext context ) throws FindException;

    public List findUsersWhoseNameStartsWith( String partialName, ServiceContext context ) throws FindException;

    public void addUsersByUsernameToGroup( List userNames, String groupName, ServiceContext context ) throws AddException;

    public void removeUsersByUsernameFromGroup( List userNames, String groupName, ServiceContext context ) throws RemoveException;
}
