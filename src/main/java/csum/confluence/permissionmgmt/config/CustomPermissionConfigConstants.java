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

package csum.confluence.permissionmgmt.config;

/**
 * @author Gary S. Weaver
 */
public interface CustomPermissionConfigConstants {

    // bandana config keys
    public static final String DELEGATE_USER_USER_MANAGER_LOCATION = "ext.delegateusermgmt.jira.user.manager.location.value"; //will indicate if JIRA or CONFLUENCE does User Management for Confluence Wiki.
    public static final String DELEGATE_USER_MGMT_JIRA_JNDI_KEY = "ext.delegateusermgmt.jira.jndi.value";
    public static final String DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY = "ext.delegateusermgmt.isldapauthpresent.value";
    public static final String DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY = "ext.delegateusermgmt.company.ldap.url.value";
    public static final String DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY = "ext.delegateusermgmt.company.ldap.base.dn.value";
    public static final String DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT = "ext.delegateusermgmt.maxuserids.limit.value";
    public static final String DELEGATE_USER_MGMT_MAXGROUPIDS_LIMIT = "ext.delegateusermgmt.maxgroupids.limit.value";
    public static final String DELEGATE_USER_MGMT_PLUGIN_STATUS = "ext.delegateusermgmt.plugin.status";
    public static final String DELEGATE_USER_MGMT_USERGROUPS_MATCHING_PATTERN = "ext.delegateusermgmt.usergroups.matching.pattern";
    public static final String DELEGATE_USER_MGMT_DOWNTIME_MESSAGE = "ext.delegateusermgmt.downtime.message.value";
    public static final String DELEGATE_USER_MGMT_GROUP_ACTIONS_PERMITTED = "ext.delegateusermgmt.group.actions.permitted.value";
    public static final String DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_PREFIX_PATTERN = "ext.delegateusermgmt.new.group.name.creation.prefix.pattern";
    public static final String DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_SUFFIX_PATTERN = "ext.delegateusermgmt.new.group.name.creation.suffix.pattern";
    public static final String DELEGATE_USER_MGMT_USER_SEARCH_ENABLED = "ext.delegateusermgmt.user.search.enabled";
    public static final String DELEGATE_USER_MGMT_JIRA_SOAP_URL = "ext.delegateusermgmt.jira.soap.url";
    public static final String DELEGATE_USER_MGMT_JIRA_SOAP_USERNAME = "ext.delegateusermgmt.jira.soap.username";
    public static final String DELEGATE_USER_MGMT_JIRA_SOAP_PASSWORD = "ext.delegateusermgmt.jira.soap.password";
    public static final String DELEGATE_USER_MGMT_PROVIDER_TYPE = "ext.delegateusermgmt.provider.type";
    public static final String DELEGATE_USER_MGMT_LDAP_USER_ID_ATTRIBUTE = "ext.delegateusermgmt.ldap,user.id.attribute";
    public static final String DELEGATE_USER_MGMT_LDAP_EMAIL_ATTRIBUTE = "ext.delegateusermgmt.ldap.email.attribute";
    public static final String DELEGATE_USER_MGMT_LDAP_NAME_ATTRIBUTE = "ext.delegateusermgmt.ldap.name.attribute";
    public static final String DELEGATE_USER_MGMT_LDAP_PROVIDER_FULLY_QUALIFIED_CLASSNAME = "ext.delegateusermgmt.ldap.provider.fully.qualified.classname";
    public static final String DELEGATE_USER_MGMT_PERSONAL_SPACE_ALLOWED = "ext.delegateusermgmt.personal.space.allowed";

    // static values
    public static final String DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE = "CONFLUENCE";
    public static final String DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE = "JIRA";

    public static final String PROVIDER_TYPE_OSUSER = "OSUSER";
    public static final String PROVIDER_TYPE_ATLASSIAN_USER = "ATLASSIAN-USER";

    public static final String YES = "YES";
    public static final String NO = "NO";

    // properties file property names
    public static final String PROPERTIES_FILE_PROPERTY_NAME_JIRA_SOAP_URL = "ext.delegateusermgmt.jira.soap.url.value";
    public static final String PROPERTIES_FILE_PROPERTY_NAME_JIRA_SOAP_USERNAME = "ext.delegateusermgmt.jira.soap.username.value";
    public static final String PROPERTIES_FILE_PROPERTY_NAME_JIRA_SOAP_PASSWORD = "ext.delegateusermgmt.jira.soap.password.value";
}
