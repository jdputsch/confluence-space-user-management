package raju.kadam.confluence.permissionmgmt.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.service.AddException;
import raju.kadam.confluence.permissionmgmt.service.FindException;
import raju.kadam.confluence.permissionmgmt.service.GroupManagementService;
import raju.kadam.confluence.permissionmgmt.service.RemoveException;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.soap.jira.JiraSoapService;
import raju.kadam.confluence.permissionmgmt.soap.jira.JiraSoapServiceServiceLocator;
import raju.kadam.confluence.permissionmgmt.soap.jira.RemoteGroup;
import raju.kadam.confluence.permissionmgmt.soap.jira.RemoteUser;
import raju.kadam.confluence.permissionmgmt.util.JiraUtil;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

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
    public List findGroups(ServiceContext context) throws FindException {
        // select groupname from groupbase

        List results = new ArrayList();

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
            return results;
        }

        try {
            ds = (DataSource) ctx.lookup(jiraJNDI);
        }
        catch (NamingException e) {
            log.error("dataSource: " + jiraJNDI + " not found.", e);
            //not able to connect to jira database.
            return results;
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

        return results;
    }

    // this is lazy and it sucks
    private RemoteGroup getRemoteGroup(String groupName, ServiceContext context) throws FindException {

        RemoteGroup result = null;

        JiraSoapService jiraSoapService = null;
        String token = null;

        try {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());
            result = jiraSoapService.getGroup(token, groupName);
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
        return result;
    }

    public void addGroup(String groupName, ServiceContext context) throws AddException {

        JiraSoapService jiraSoapService = null;
        String token = null;

        try {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());
            RemoteUser remoteUser = null;
            jiraSoapService.createGroup(token, groupName, remoteUser);
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


    public void removeGroup(String groupName, ServiceContext context) throws RemoveException {
        JiraSoapService jiraSoapService = null;
        String token = null;

        try {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());
            String swapGroupName = null;
            jiraSoapService.deleteGroup(token, groupName, swapGroupName);
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
