package raju.kadam.confluence.permissionmgmt.service;

import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import org.displaytag.pagination.PaginatedList;
import com.atlassian.user.search.page.Pager;

import java.util.List;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:41:57 AM
 */
public interface GroupManagementService {

    public Pager findGroups( ServiceContext context ) throws FindException;

    public void addGroups( List groupNames, ServiceContext context ) throws AddException;

    public void removeGroups( List groupNames, ServiceContext context ) throws RemoveException;
}
