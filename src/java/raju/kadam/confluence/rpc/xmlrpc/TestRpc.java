/*
Copyright (c) 2006, Rajendra Kadam
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.  Redistributions
in binary form must reproduce the above copyright notice, this list of
conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.  Neither the name of
Cenqua Pty Ltd nor the names of its contributors may be used to
endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package raju.kadam.confluence.rpc.xmlrpc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.sql.DataSource;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.log4j.Category;

/**
 *
 * @author Rajendra Kadam
 */
public class TestRpc
{
    private static final Category log = Category.getInstance(TestRpc.class);

    
    public static final String CONFLUENCE_URI  = "<confluence wiki base url>";
    public static final String RPC_PATH  = "/rpc/xmlrpc";

    public static final String SPACE_ADMIN_USER_NAME = "<test userid>";
    public static final String SPACE_KEY = "<test space key>";

    
    
    public static void main(String[] args)
    {
        try
        {
            // Initialise RPC Client
            XmlRpcClient rpcClient = new XmlRpcClient(CONFLUENCE_URI + RPC_PATH);

            // Login and retrieve logon token
            Vector loginParams = new Vector(1);
            loginParams.add(SPACE_ADMIN_USER_NAME);
            loginParams.add(SPACE_KEY);

            Boolean resultsBool = (Boolean) rpcClient.execute("userroleservice.verifySpaceAdminRole", loginParams);
            //Hashtable resultHash = (Hashtable)resultsVec.get(0);
            log.debug("Is this Person Space Admin? Ans -> " + resultsBool.booleanValue());
            
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (XmlRpcException e)
        {
            e.printStackTrace();
        }

    }

    public void VerifyJiraConnection()
    {
    	Connection connection = null;
		ResultSet resultSet = null;
        DataSource ds = null;
        String jndi = "java:comp/env/jdbc/JiraDS" ;
        InitialContext ctx = null;
        
		try
		{
			ctx = new InitialContext();
            try {
                ds = (DataSource) ctx.lookup(jndi);
			}
            catch (NameNotFoundException exception) {
                log.debug("dataSource: " + jndi + " not found.");
                exception.printStackTrace();
            }
            
            connection = ds.getConnection();
            String sql = "select id from userbase where username = 'rakadam' ";
			PreparedStatement statement = null;

			try {
				statement = connection.prepareStatement(sql);
                resultSet = statement.executeQuery();
                while(resultSet.next())
                {
                	log.debug(resultSet.getString(1));
                }
            }
            catch (SQLException sqlException) { // no data in result set due to update or insert operation
            	sqlException.printStackTrace();
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
				if (connection != null)
				{
					connection.close();
				}
			}
			catch(Exception discard) {}
		}
	}
}
