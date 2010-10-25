/**
 * Copyright (c) 2007-2010, Custom Space User Management Plugin Development Team
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
 *     * Neither the name of the Custom Space User Management Plugin Development Team
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

package csum.confluence.permissionmgmt.service;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.service.exception.ServiceException;
import csum.confluence.permissionmgmt.service.impl.ConfluenceGroupManagementService;
import csum.confluence.permissionmgmt.service.impl.ConfluenceUserManagementService;
import csum.confluence.permissionmgmt.service.impl.JiraSoapGroupManagementService;
import csum.confluence.permissionmgmt.service.impl.JiraSoapUserManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Gary S. Weaver
 */
public class CustomPermissionServiceManager {

    private Log log = LogFactory.getLog(this.getClass());

    private ConfluenceGroupManagementService confluenceGroupManagementService;
    private ConfluenceUserManagementService confluenceUserManagementService;
    private JiraSoapGroupManagementService jiraSoapGroupManagementService;
    private JiraSoapUserManagementService jiraSoapUserManagementService;
    private CustomPermissionConfiguration customPermissionConfiguration;

    public GroupManagementService getGroupManagementService(ConfluenceActionSupport action) throws ServiceException {
        CustomPermissionConfiguration config = getCustomPermissionConfiguration();
        String userManagerLocation = config.getUserManagerLocation();

        if (userManagerLocation == null) {
            throw new ServiceException(action.getText("csum.manager.error.invalidusermanager"));
        } else
        if (userManagerLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE)) {
            return confluenceGroupManagementService;
        } else
        if (userManagerLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE)) {
            return jiraSoapGroupManagementService;
        } else {
            throw new ServiceException(action.getText("csum.manager.error.invalidusermanager"));
        }
    }

    public UserManagementService getUserManagementService(ConfluenceActionSupport action) throws ServiceException {
        CustomPermissionConfiguration config = getCustomPermissionConfiguration();
        String userManagerLocation = config.getUserManagerLocation();

        if (userManagerLocation == null) {
            throw new ServiceException(action.getText("csum.manager.error.invalidusermanager"));
        } else
        if (userManagerLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE)) {
            return confluenceUserManagementService;
        } else
        if (userManagerLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE)) {
            return jiraSoapUserManagementService;
        } else {
            throw new ServiceException(action.getText("csum.manager.error.invalidusermanager"));
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

    public JiraSoapGroupManagementService getJiraGroupManagementService() {
        return jiraSoapGroupManagementService;
    }

    public void setJiraSoapGroupManagementService(JiraSoapGroupManagementService jiraSoapGroupManagementService) {
        this.jiraSoapGroupManagementService = jiraSoapGroupManagementService;
    }

    public JiraSoapUserManagementService getJiraSoapUserManagementService() {
        return jiraSoapUserManagementService;
    }

    public void setJiraSoapUserManagementService(JiraSoapUserManagementService jiraSoapUserManagementService) {
        this.jiraSoapUserManagementService = jiraSoapUserManagementService;
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }
}
