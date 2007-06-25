package raju.kadam.confluence.permissionmgmt.service;

import com.atlassian.user.Group;
import com.atlassian.confluence.spaces.Space;

import java.util.List;

import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:41:57 AM
 */
public interface GroupManagementService {

    public List findGroups( ServiceContext context ) throws FindException;

    public void addGroup( String identifier, ServiceContext context ) throws AddException;

    public void removeGroup( String groupName, ServiceContext context ) throws RemoveException;
}
