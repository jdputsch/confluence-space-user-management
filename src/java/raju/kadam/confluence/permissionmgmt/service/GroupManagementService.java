package raju.kadam.confluence.permissionmgmt.service;

import com.atlassian.user.Group;
import com.atlassian.confluence.spaces.Space;

import java.util.List;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:41:57 AM
 */
public interface GroupManagementService {

    public List findGroups( Space space );

    public Group createGroup( String identifier, Space space );

    public void deleteGroup( Group group, Space space );
}
