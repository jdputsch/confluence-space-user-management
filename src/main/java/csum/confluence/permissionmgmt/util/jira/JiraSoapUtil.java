/**
 * Copyright (c) 2007-2012, Custom Space User Management Plugin Development Team
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

package csum.confluence.permissionmgmt.util.jira;

import csum.confluence.permissionmgmt.config.CustomPermissionConfigurable;
import csum.confluence.permissionmgmt.service.exception.ServiceAuthenticationException;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapService;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapServiceServiceLocator;
import csum.confluence.permissionmgmt.util.logging.LogUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Gary S. Weaver
 */
public class JiraSoapUtil {

    private static Log log = LogFactory.getLog(JiraSoapUtil.class);

    public static JiraServiceAuthenticationContext login(ServiceContext serviceContext) throws ServiceAuthenticationException {
        JiraServiceAuthenticationContext jiraServiceAuthenticationContext = new JiraServiceAuthenticationContext();
        try {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            CustomPermissionConfigurable config = serviceContext.getCustomPermissionConfigurable();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(config.getJiraSoapUrl());
            JiraSoapService jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            jiraServiceAuthenticationContext.setJiraSoapService(jiraSoapService);
            jiraServiceAuthenticationContext.setToken(jiraSoapService.login(config.getJiraSoapUsername(), config.getJiraSoapPassword()));
        }
        catch (Throwable t) {
            LogUtil.errorWithRemoteUserInfo(log, "Failed Jira SOAP Authentication!", t);
            throw new ServiceAuthenticationException("Failed Jira SOAP Authentication!", t);
        }

        return jiraServiceAuthenticationContext;
    }

    public static void logout(JiraServiceAuthenticationContext jiraServiceAuthenticationContext) throws ServiceAuthenticationException {
        try {
            jiraServiceAuthenticationContext.getJiraSoapService().logout(jiraServiceAuthenticationContext.getToken());
        }
        catch (Throwable t) {
            LogUtil.errorWithRemoteUserInfo(log, "Failed Jira SOAP Authentication!", t);
            throw new ServiceAuthenticationException("Failed Jira SOAP Authentication!", t);
        }
    }
}
