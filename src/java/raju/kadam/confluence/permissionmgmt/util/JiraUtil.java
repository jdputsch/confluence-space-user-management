package raju.kadam.confluence.permissionmgmt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcClient;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;

import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.CustomPermissionManagerActionContext;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 19, 2007
 * Time: 4:36:57 PM
 */
public class JiraUtil {

    public static final String RPC_PATH  = "/rpc/xmlrpc";

    private static Log log = LogFactory.getLog(JiraUtil.class);


    //For requester Space Administrator, get the id value from jira schema
    //This value will be passed along with data on which we want to act.
    //If this id matches with one that is reterived there, then this user request is really coming from Authentic Space Administrator related to given space.
    public static String getSecretId(String requesterUserId, String jiraJNDILookupKey)
    {
    	String secretId = null;

        Connection connection = null;
        PreparedStatement statement = null;
		ResultSet resultSet = null;
        DataSource ds = null;
        String jiraJNDI = "java:comp/env/" + jiraJNDILookupKey ;
        InitialContext ctx = null;

		try
		{
			ctx = new InitialContext();
            try {
                ds = (DataSource) ctx.lookup(jiraJNDI);
			}
            catch (NameNotFoundException exception) {
                log.debug("dataSource: " + jiraJNDI + " not found.");
                exception.printStackTrace();
                //not able to connect to jira database.
               return secretId;
            }

            connection = ds.getConnection();
            String sql = "select id from userbase where username = ?";
			statement = null;

			try {
				statement = connection.prepareStatement(sql);
				statement.setString(1,requesterUserId);
                resultSet = statement.executeQuery();
                if(resultSet != null && resultSet.next())
                {
                	secretId = resultSet.getString(1);
                }
            }
            catch (SQLException sqlException) {
            	// this case shouldn't come, as requester User's verification is already done successfully.
            	sqlException.printStackTrace();
                //not able to connect to jira database.
            }
		}
    	catch(Exception exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			try
			{
				if (resultSet != null)
				{
					resultSet.close();
				}

				if (statement != null)
				{
					statement.close();
				}

				if (connection != null)
				{
					connection.close();
				}
			}
			catch(Exception discard) {}
		}

		//log.debug("Secret id for user " + requesterUserId + " is " + secretId);

	  return secretId;

    }

    //call Jira RPC Service
    public static RpcResponse callJiraRPCService(CustomPermissionManagerActionContext context, CustomPermissionConfiguration config)
    //call Jira RPC Service
//    public void callJiraRPCService(String actionPerformerUser,
//                                                                String spaceKey,
//                                                                List userIdList,
//                                                                List groupList,
//                                                                String adminAction,
//                                                                String secretId)
    {
        RpcResponse response = new RpcResponse();

        /*
        String tempUserIDs = "";
               for(Iterator itr = userIdList.iterator(); itr.hasNext();)
               {
                       tempUserIDs +=(String) itr.next() + ",";
               }

        String groupNames = "";
               for(Iterator itr = groupList.iterator(); itr.hasNext();)
               {
                       groupNames +=(String) itr.next()+ ",";
               }

               log.debug("Space Admin - " + actionPerformerUser);
               log.debug("Space Key - " + spaceKey);
               log.debug("Input Users - " + tempUserIDs);
               log.debug("Input Group names - " + groupNames);
        log.debug("AdminAction - " + adminAction);
        log.debug("Secretid - " + secretId );
        log.debug("getJiraJNDILookupKey - " + getJiraJNDILookupKey());
        log.debug("getLdapAuthKey() - " + getIsLdapAuthUsed() );
        log.debug("getJiraUrl() - " + getJiraUrl());
        log.debug("getCompanyLDAPUrl - " + getCompanyLDAPUrl());
        log.debug("getCompanyLDAPBaseDN - " + getCompanyLDAPBaseDN());
        */

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
            String adminAction = context.getAdminAction();
            if(adminAction.equalsIgnoreCase("AddToGroups"))
            {
                // Login and retrieve logon token
                Vector rpcParams = new Vector(10);
                //Enter User who is sending this request to RPC call.
                rpcParams.add(context.getLoggedInUser());
                //Space Name.
                rpcParams.add(context.getSpaceKey());
                //Get the list of user ids on which we want to act upon.
                rpcParams.add(context.getUsersToAddList());
                //Groups to which users need to associate.
                rpcParams.add(context.getSelectedGroups());
                //Jira Datasource
                rpcParams.add(config.getJiraJNDILookupKey());
                //Confluence Url
                rpcParams.add(ConfluenceUtil.getConfluenceUrl(context.getBootstrapManager()));
                //Secret Id
                rpcParams.add(context.getSecretId());

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
            }
            else if(adminAction.equalsIgnoreCase("RemoveFromGroups"))
            {
                // Login and retrieve logon token
                Vector rpcParams = new Vector(10);
                //Enter User who is sending this request to RPC call.
                rpcParams.add(context.getLoggedInUser());
                //Space Name.
                rpcParams.add(context.getSpaceKey());
                //Get the list of user ids on which we want to act upon.
                rpcParams.add(context.getUsersToRemoveList());
                //Groups to which users need to associate.
                rpcParams.add(context.getSelectedGroups());
                //Jira Datasource
                rpcParams.add(config.getJiraJNDILookupKey());
                //Confluence Url
                rpcParams.add(ConfluenceUtil.getConfluenceUrl(context.getBootstrapManager()));
                //Secret Id
                rpcParams.add(context.getSecretId());

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
}
