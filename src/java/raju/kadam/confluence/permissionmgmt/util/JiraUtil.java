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
}
