package raju.kadam.confluence.permissionmgmt.service;

import com.atlassian.spring.container.ContainerManager;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.service.impl.ConfluenceGroupManagementService;
import raju.kadam.confluence.permissionmgmt.service.impl.ConfluenceUserManagementService;
import raju.kadam.confluence.permissionmgmt.service.impl.JiraGroupManagementService;
import raju.kadam.confluence.permissionmgmt.service.impl.JiraUserManagementService;
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
    private JiraGroupManagementService jiraGroupManagementService;
    private JiraUserManagementService jiraUserManagementService;
    private CustomPermissionConfiguration customPermissionConfiguration;

    public CustomPermissionServiceManager() {
        //confluenceGroupManagementService = (ConfluenceGroupManagementService) ContainerManager.getComponent("confluenceGroupManagementService");
        //confluenceUserManagementService = (ConfluenceUserManagementService) ContainerManager.getComponent("confluenceUserManagementService");
        //jiraGroupManagementService = (JiraGroupManagementService) ContainerManager.getComponent("jiraGroupManagementService");
        //jiraUserManagementService = (JiraUserManagementService) ContainerManager.getComponent("jiraUserManagementService");
        //customPermissionConfiguration = (CustomPermissionConfiguration) ConfluenceUtil.loadComponentWithRetry("customPermissionConfiguration");
        log.debug("CustomPermissionServiceManager created");
    }

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
            return jiraGroupManagementService;
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
            return jiraUserManagementService;
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

    public JiraGroupManagementService getJiraGroupManagementService() {
        return jiraGroupManagementService;
    }

    public void setJiraGroupManagementService(JiraGroupManagementService jiraGroupManagementService) {
        this.jiraGroupManagementService = jiraGroupManagementService;
    }

    public JiraUserManagementService getJiraUserManagementService() {
        return jiraUserManagementService;
    }

    public void setJiraUserManagementService(JiraUserManagementService jiraUserManagementService) {
        this.jiraUserManagementService = jiraUserManagementService;
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }
}
