package raju.kadam.confluence.permissionmgmt.service;

import com.atlassian.spring.container.ContainerManager;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.service.impl.*;
import raju.kadam.confluence.permissionmgmt.util.ConfluenceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 22, 2007
 * Time: 10:53:35 AM
 */
public class CustomPermissionServiceManager {

    private Log log = LogFactory.getLog(this.getClass());

    private ConfluenceGroupManagementService confluenceGroupManagementService;
    private ConfluenceUserManagementService confluenceUserManagementService;
    private JiraSoapGroupManagementService jiraSoapGroupManagementService;
    private JiraSoapUserManagementService jiraSoapUserManagementService;
    private CustomPermissionConfiguration customPermissionConfiguration;

    public GroupManagementService getGroupManagementService() throws ServiceException {
        CustomPermissionConfiguration config = getCustomPermissionConfiguration();
        String userManagerLocation = config.getUserManagerLocation();

        if ( userManagerLocation==null )
        {
            throw new ServiceException(ErrorReason.INVALID_USER_MANAGER_LOCATION);
        }
        else if(userManagerLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE))
    	{
            return confluenceGroupManagementService;
    	}
    	else if(userManagerLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE))
    	{
            return jiraSoapGroupManagementService;
        }
        else {
            throw new ServiceException(ErrorReason.INVALID_USER_MANAGER_LOCATION);
        }
    }

    public UserManagementService getUserManagementService() throws ServiceException {
        CustomPermissionConfiguration config = getCustomPermissionConfiguration();
        String userManagerLocation = config.getUserManagerLocation();

        if ( userManagerLocation==null )
        {
            throw new ServiceException(ErrorReason.INVALID_USER_MANAGER_LOCATION);
        }
        else if(userManagerLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE))
    	{
            return confluenceUserManagementService;
    	}
    	else if(userManagerLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE))
    	{
            return jiraSoapUserManagementService;
        }
        else {
            throw new ServiceException(ErrorReason.INVALID_USER_MANAGER_LOCATION);
        }
    }

    public ConfluenceGroupManagementService getConfluenceGroupManagementService() {
        return confluenceGroupManagementService;
    }

    public void setConfluenceGroupManagementService(ConfluenceGroupManagementService confluenceGroupManagementService) {
        this.confluenceGroupManagementService = confluenceGroupManagementService;
    }

    public ConfluenceUserManagementService getConfluenceUserManagementService() {
        return confluenceUserManagementService;
    }

    public void setConfluenceUserManagementService(ConfluenceUserManagementService confluenceUserManagementService) {
        this.confluenceUserManagementService = confluenceUserManagementService;
    }

    public JiraSoapGroupManagementService getJiraGroupManagementService() {
        return jiraSoapGroupManagementService;
    }

    public void setJiraSoapGroupManagementService(JiraSoapGroupManagementService jiraSoapGroupManagementService) {
        this.jiraSoapGroupManagementService = jiraSoapGroupManagementService;
    }

    public JiraSoapUserManagementService getJiraSoapUserManagementService() {
        return jiraSoapUserManagementService;
    }

    public void setJiraSoapUserManagementService(JiraSoapUserManagementService jiraSoapUserManagementService) {
        this.jiraSoapUserManagementService = jiraSoapUserManagementService;
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }
}
