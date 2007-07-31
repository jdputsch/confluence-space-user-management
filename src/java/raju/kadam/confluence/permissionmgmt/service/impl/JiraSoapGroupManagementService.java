/**
* Copyright (c) 2007, Custom Space Usergroups Manager Development Team
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the Custom Space Usergroups Manager Development Team nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE CUSTOM SPACE USERGROUPS MANAGER DEVELOPMENT TEAM ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE CUSTOM SPACE USERGROUPS MANAGER DEVELOPMENT TEAM BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package raju.kadam.confluence.permissionmgmt.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.service.exception.AddException;
import raju.kadam.confluence.permissionmgmt.service.exception.FindException;
import raju.kadam.confluence.permissionmgmt.service.exception.RemoveException;
import raju.kadam.confluence.permissionmgmt.service.GroupManagementService;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.soap.jira.JiraSoapService;
import raju.kadam.confluence.permissionmgmt.soap.jira.JiraSoapServiceServiceLocator;
import raju.kadam.confluence.permissionmgmt.soap.jira.RemoteUser;
import raju.kadam.confluence.permissionmgmt.util.jira.JiraUtil;
import raju.kadam.confluence.permissionmgmt.util.StringUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.DefaultPager;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 5, 2007
 * Time: 1:45:36 PM
 */
public class JiraSoapGroupManagementService implements GroupManagementService {

    private Log log = LogFactory.getLog(this.getClass());

    private CustomPermissionConfiguration customPermissionConfiguration;
    private JiraSoapUserManagementService jiraSoapUserManagementService;

    //TODO: request getGroups(String token) as a feature in Jira's soap service
    public Pager findGroups(ServiceContext context) throws FindException {
        log.debug("findGroups() called.");
        // select groupname from groupbase

        List results = new ArrayList();
        Pager pager = new DefaultPager(results);

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        DataSource ds = null;
        String jiraJNDI = "java:comp/env/" + getCustomPermissionConfiguration().getJiraJNDILookupKey();
        InitialContext ctx = null;

        try {
            ctx = new InitialContext();
        }
        catch (NamingException e) {
            log.error("Could not get JNDI context.", e);
            return pager;
        }

        try {
            ds = (DataSource) ctx.lookup(jiraJNDI);
        }
        catch (NamingException e) {
            log.error("dataSource: " + jiraJNDI + " not found.", e);
            //not able to connect to jira database.
            return pager;
        }

        try {
            connection = ds.getConnection();
        }
        catch (SQLException e) {
            log.error("Couldn't get connection to Jira DB via JNDI lookup using '" + jiraJNDI + "'", e);
        }

        String sql = "select groupname from groupbase";

        try {
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String groupName = resultSet.getString(0);
                if ( groupName != null ) {
                    results.add(groupName);
                }
            }
        }
        catch (SQLException e) {
            log.error("Failure in statement '" + sql + "' using Jira DB via JNDI lookup using '" + jiraJNDI + "'", e);
        }

        return pager;
    }

    public void addGroups(List groupNames, ServiceContext context) throws AddException {
        log.debug("addGroup() called. groupNames='" + StringUtil.convertCollectionToCommaDelimitedString(groupNames) + "'");
        JiraSoapService jiraSoapService = null;
        String token = null;

        try {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());
            RemoteUser remoteUser = null;
            for (int i=0; i<groupNames.size(); i++) {
                String groupName = (String)groupNames.get(i);
                jiraSoapService.createGroup(token, groupName, remoteUser);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        finally {
            if (token != null) {
                try {
                    jiraSoapService.logout(token);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }


    public void removeGroups(List groupNames, ServiceContext context) throws RemoveException {
        log.debug("removeGroup() called. groupName='" + StringUtil.convertCollectionToCommaDelimitedString(groupNames) + "'");
        JiraSoapService jiraSoapService = null;
        String token = null;

        try {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());
            String swapGroupName = null;
            for (int i=0; i<groupNames.size(); i++) {
                String groupName = (String)groupNames.get(i);
                jiraSoapService.deleteGroup(token, groupName, swapGroupName);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        finally {
            if (token != null) {
                try {
                    jiraSoapService.logout(token);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }

    public JiraSoapUserManagementService getJiraSoapUserManagementService() {
        return jiraSoapUserManagementService;
    }

    public void setJiraSoapUserManagementService(JiraSoapUserManagementService jiraSoapUserManagementService) {
        this.jiraSoapUserManagementService = jiraSoapUserManagementService;
    }
}
