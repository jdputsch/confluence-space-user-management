package raju.kadam.confluence.permissionmgmt.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import raju.kadam.confluence.permissionmgmt.service.*;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;

import java.util.List;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 22, 2007
 * Time: 8:31:07 AM
 */
public class JiraGroupManagementService implements GroupManagementService {

    private Log log = LogFactory.getLog(this.getClass());

    public JiraGroupManagementService() {
        log.debug("JiraGroupManagementService start constructor");
        log.debug("JiraGroupManagementService end constructor");
    }

    public List findGroups( ServiceContext context ) throws FindException {
        throw new FindException(ErrorReason.UNSUPPORTED_FEATURE);
    }

    public void addGroup( String identifier, ServiceContext context ) throws AddException {
        throw new AddException(ErrorReason.UNSUPPORTED_FEATURE);
    }

    public void removeGroup( String groupName, ServiceContext context ) throws RemoveException {
        throw new RemoveException(ErrorReason.UNSUPPORTED_FEATURE);
    }
}
