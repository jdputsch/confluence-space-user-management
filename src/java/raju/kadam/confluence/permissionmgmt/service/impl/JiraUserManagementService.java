package raju.kadam.confluence.permissionmgmt.service.impl;

import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.spring.container.ContainerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcClient;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.service.*;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.util.ConfluenceUtil;
import raju.kadam.confluence.permissionmgmt.util.JiraUtil;
import raju.kadam.confluence.permissionmgmt.util.RpcResponse;

import java.util.List;
import java.util.Vector;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 22, 2007
 * Time: 8:31:21 AM
 */
public class JiraUserManagementService implements UserManagementService {

    public static final String RPC_PATH  = "/rpc/xmlrpc";

    private Log log = LogFactory.getLog(this.getClass());

    private SettingsManager settingsManager;
    private CustomPermissionConfiguration customPermissionConfiguration;

    public JiraUserManagementService() {
        log.debug("JiraUserManagementService start constructor");
        settingsManager = (SettingsManager) ContainerManager.getComponent("settingsManager");
        //customPermissionConfiguration = (CustomPermissionConfiguration) ConfluenceUtil.loadComponentWithRetry("customPermissionConfiguration");
        log.debug("JiraUserManagementService end constructor");
    }


    // member getter/setters

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    // old methods

        //call Jira RPC Service
    public RpcResponse addUsersToGroups(List users, List groups, ServiceContext context) throws ServiceException {
        RpcResponse response = new RpcResponse();

        CustomPermissionConfiguration config = getCustomPermissionConfiguration();

        log.debug("Calling JiraRPCService");

        Boolean isLDAPLookupAvailable = new Boolean(config.getLdapAuthUsed().equals(CustomPermissionConfigConstants.YES) ? true : false);
        log.debug( "isLDAPLookupAvailable - " + isLDAPLookupAvailable);

        //Ok time to call Jira RPC plugin as we have all data that needs to be processed.
        try
        {
            // Initialise RPC Client
            XmlRpcClient rpcClient = new XmlRpcClient(config.getJiraUrl() + RPC_PATH);
            log.debug( "Making call to Jira RPC Client");


            List results = null;

            // Login and retrieve logon token
            Vector rpcParams = new Vector(10);
            //Enter User who is sending this request to RPC call.
            rpcParams.add(context.getLoggedInUser());
            //Space Name.
            rpcParams.add(context.getSpace().getKey());
            //Get the list of user ids on which we want to act upon.
            rpcParams.add(users);
            //Groups to which users need to associate.
            rpcParams.add(groups);
            //Jira Datasource
            rpcParams.add(config.getJiraJNDILookupKey());
            //Confluence Url
            rpcParams.add(ConfluenceUtil.getConfluenceUrl(getSettingsManager()));
            //Secret Id
            rpcParams.add(JiraUtil.getSecretId(context.getLoggedInUser(), config.getJiraJNDILookupKey()));

            //If LDAP Lookup available, then pass that info for user creation
            //Note if isLDAPLookupAvailable is false, then LDAPurl, BaseDN should contain empty strings "" else xml-rpc will throw error!
            rpcParams.add(isLDAPLookupAvailable);
            rpcParams.add(config.getCompanyLDAPUrl());
            rpcParams.add(config.getCompanyLDAPBaseDN());

            log.debug( "Adding users to JIRA group");

            results = (List) rpcClient.execute("delegateusermgmt.addUsersToGroups", rpcParams);

            //depending upon result of action, display messages.
            if( results != null && results.size() > 0 ) {
                log.debug("Result is - " + results.get(0));
                if (((String)results.get(0)).startsWith("Error"))
                {
                    response.setError(true);
                }
            }


            //depending upon result of action, display messages.
            if( results != null && results.size() > 0 && ((String)results.get(0)).startsWith("Error"))
            {
                response.setError(true);
            }

            response.setMessages(results);
        }
        catch (Exception e)
        {
            response.setError(true);
            Vector msgs = new Vector();
            msgs.add(e.getMessage());
            response.setMessages(msgs);
            e.printStackTrace();
        }

        return response;
    }

    //call Jira RPC Service
    public RpcResponse removeUsersFromGroups(List users, List groups, ServiceContext context) throws ServiceException {

        RpcResponse response = new RpcResponse();

        CustomPermissionConfiguration config = getCustomPermissionConfiguration();

        log.debug("Calling JiraRPCService");

        Boolean isLDAPLookupAvailable = new Boolean(config.getLdapAuthUsed().equals(CustomPermissionConfigConstants.YES) ? true : false);
        log.debug( "isLDAPLookupAvailable - " + isLDAPLookupAvailable);

        try
        {
            // Initialise RPC Client
            XmlRpcClient rpcClient = new XmlRpcClient(config.getJiraUrl() + RPC_PATH);
            log.debug( "Making call to Jira RPC Client");


            List results = null;
            // Login and retrieve logon token
            Vector rpcParams = new Vector(10);
            //Enter User who is sending this request to RPC call.
            rpcParams.add(context.getLoggedInUser());
            //Space Name.
            rpcParams.add(context.getSpace().getKey());
            //Get the list of user ids on which we want to act upon.
            rpcParams.add(users);
            //Groups to which users need to associate.
            rpcParams.add(groups);
            //Jira Datasource
            rpcParams.add(config.getJiraJNDILookupKey());
            //Confluence Url
            rpcParams.add(ConfluenceUtil.getConfluenceUrl(getSettingsManager()));
            //Secret Id
            rpcParams.add(JiraUtil.getSecretId(context.getLoggedInUser(), config.getJiraJNDILookupKey()));

            log.debug( "Removing users from JIRA group");

            results = (List)rpcClient.execute("delegateusermgmt.removeUsersFromGroups", rpcParams);

            //depending upon result of action, display messages.
            if( results != null && results.size() > 0 ) {
                log.debug("Result is - " + results.get(0));
                if (((String)results.get(0)).startsWith("Error"))
                {
                    response.setError(true);
                }
            }


            //depending upon result of action, display messages.
            if( results != null && results.size() > 0 && ((String)results.get(0)).startsWith("Error"))
            {
                response.setError(true);
            }

            response.setMessages(results);
        }
        catch (Exception e)
        {
            response.setError(true);
            Vector msgs = new Vector();
            msgs.add(e.getMessage());
            response.setMessages(msgs);
            e.printStackTrace();
        }

        return response;
    }


    public List findUsers(AdvancedUserQuery advancedUserQuery, ServiceContext context) throws FindException {
        throw new FindException(ErrorReason.UNSUPPORTED_FEATURE);
    }

    public List findUsersForGroup(String groupName, ServiceContext context) throws FindException {
        throw new FindException(ErrorReason.UNSUPPORTED_FEATURE);
    }

    public List findUsersWhoseNameStartsWith(String partialName, ServiceContext context) throws FindException {
        throw new FindException(ErrorReason.UNSUPPORTED_FEATURE);
    }

    public void addUsersToGroup(List userNames, String groupName, ServiceContext context) throws AddException {
        throw new AddException(ErrorReason.UNSUPPORTED_FEATURE);
    }

    public void removeUsersFromGroup(List userNames, String groupName, ServiceContext context) throws RemoveException {
        throw new RemoveException(ErrorReason.UNSUPPORTED_FEATURE);
    }
}
