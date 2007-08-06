/**
 * Copyright (c) 2007, Custom Space Usergroups Manager Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Custom Space Usergroups Manager Development Team
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package raju.kadam.confluence.permissionmgmt.util.jira;

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
import raju.kadam.confluence.permissionmgmt.util.PropsUtil;

/**
 * @author Gary S. Weaver
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