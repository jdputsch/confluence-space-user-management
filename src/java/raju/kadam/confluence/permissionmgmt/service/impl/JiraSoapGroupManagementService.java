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
import raju.kadam.confluence.permissionmgmt.soap.jira.RemoteGroup;
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
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.spaces.Space;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class JiraSoapGroupManagementService extends ConfluenceGroupManagementService {

    private Log log = LogFactory.getLog(this.getClass());

    private CustomPermissionConfiguration customPermissionConfiguration;
    private JiraSoapUserManagementService jiraSoapUserManagementService;

    // note: findGroups()... are in ConfluenceGroupManagementService, as Confluence has read-only access to JIRA

    public void addGroups(List groupNames, ServiceContext context) throws AddException {
        log.debug("addGroups() called. groupName='" + StringUtil.convertCollectionToCommaDelimitedString(groupNames) + "'");
        Space space = context.getSpace();
        
        JiraSoapService jiraSoapService = null;
        String token = null;

        List success = new ArrayList();
        List alreadyExisted = new ArrayList();

        try {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());
            RemoteUser remoteUser = null;
            for (int i=0; i<groupNames.size(); i++) {
                String groupName = (String)groupNames.get(i);

                if (userAccessor.getGroup(groupName) == null) {
                    RemoteGroup vGroup = jiraSoapService.createGroup(token, groupName, remoteUser);
                    log.debug("created " + groupName);
                    success.add(groupName);

                    //If group exists then set all required permissions
                    if (vGroup != null)
                    {
                        SpacePermission perm = new SpacePermission(SpacePermission.VIEWSPACE_PERMISSION, space, vGroup.getName());
                        space.addPermission(perm);
                        log.debug("added viewspace perm to " + groupName);
                    }
                }
                else {
                    alreadyExisted.add(groupName);
                }
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

        if (alreadyExisted.size()>0) {
            String msg = "";
            String concat = "";
            if (alreadyExisted.size()>0) {
                msg += context.getText("groups.already.existed") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(alreadyExisted) + ".";
                concat = " ";
            }

            if (success.size()>0) {
                msg += concat;
                msg += context.getText("error.groupAddSuccess") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(success) + ".";
            }

            throw new AddException(msg);
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
