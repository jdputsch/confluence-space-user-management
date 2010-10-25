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

package csum.confluence.permissionmgmt.config;

/**
 * Constants from v1.1 of this plugin
 *
 * @author Gary S. Weaver
 */
public interface LegacyConfigConstants {

    public static final String DELEGATE_USER_USER_MANAGER_LOCATION = "ext.delegateusermgmt.jira.user.manager.location.value"; //will indicate if JIRA or CONFLUENCE does User Management for Confluence Wiki.
    public static final String DELEGATE_USER_MGMT_JIRA_URL = "ext.delegateusermgmt.jira.url.value";
    public static final String DELEGATE_USER_MGMT_JIRA_JNDI_KEY = "ext.delegateusermgmt.jira.jndi.value";
    public static final String DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY = "ext.delegateusermgmt.isldapauthpresent.value";
    public static final String DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY = "ext.delegateusermgmt.company.ldap.url.value";
    public static final String DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY = "ext.delegateusermgmt.company.ldap.base.dn.value";
    public static final String DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT = "ext.delegateusermgmt.maxuserids.limit.value";
    public static final String DELEGATE_USER_MGMT_PLUGIN_STATUS = "ext.delegateusermgmt.plugin.status";
    public static final String DELEGATE_USER_MGMT_USER_MATCHING_PATTERN = "ext.delegateusermgmt.user.matching.pattern";
    public static final String DELEGATE_USER_MGMT_DOWNTIME_MESSAGE = "ext.delegateusermgmt.downtime.message.value";

    public static final String DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE = "CONFLUENCE";
    public static final String DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE = "JIRA";

    public static final String DELEGATE_USER_LDAP_AUTH_KEY_YES_VALUE = "YES";
    public static final String DELEGATE_USER_LDAP_AUTH_KEY_NO_VALUE = "NO";
}
