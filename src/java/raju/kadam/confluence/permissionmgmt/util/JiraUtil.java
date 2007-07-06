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
import java.io.IOException;

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

    public static String getJiraSoapUrl() throws IOException {
        return PropsUtil.getProperty(CustomPermissionConfigConstants.PROPERTIES_FILE_PROPERTY_NAME_JIRA_SOAP_URL);
    }

    public static String getJiraSoapUsername() throws IOException {
        return PropsUtil.getProperty(CustomPermissionConfigConstants.PROPERTIES_FILE_PROPERTY_NAME_JIRA_SOAP_USERNAME);
    }

    public static String getJiraSoapPassword() throws IOException {
        return PropsUtil.getProperty(CustomPermissionConfigConstants.PROPERTIES_FILE_PROPERTY_NAME_JIRA_SOAP_PASSWORD);
    }
}
