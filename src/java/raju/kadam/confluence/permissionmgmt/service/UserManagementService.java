package raju.kadam.confluence.permissionmgmt.service;

import java.util.List;

import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;
import com.atlassian.user.search.page.Pager;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:37:32 AM
 */
public interface UserManagementService {

    public AdvancedUserQueryResults findUsers( AdvancedUserQuery advancedUserQuery, ServiceContext context ) throws FindException;

    public Pager findUsersForGroup( String groupName, ServiceContext context ) throws FindException;

    public Pager findUsersWhoseNameStartsWith( String partialName, ServiceContext context ) throws FindException;

    public void addUsersByUsernameToGroups( List userNames, List groupNames, ServiceContext context ) throws AddException;

    public void removeUsersByUsernameFromGroups( List userNames, List groupNames, ServiceContext context ) throws RemoveException;

    public boolean isMemberOf(String userName, String groupName) throws FindException;
}
