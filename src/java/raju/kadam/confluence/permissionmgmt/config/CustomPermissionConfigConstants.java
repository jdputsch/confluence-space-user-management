package raju.kadam.confluence.permissionmgmt.config;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 3:06:20 PM
 */
public interface CustomPermissionConfigConstants {

    public static final String DELEGATE_USER_USER_MANAGER_LOCATION = "ext.delegateusermgmt.jira.user.manager.location.value"; //will indicate if JIRA or CONFLUENCE does User Management for Confluence Wiki.
    public static final String DELEGATE_USER_MGMT_JIRA_URL = "ext.delegateusermgmt.jira.url.value";
    public static final String DELEGATE_USER_MGMT_JIRA_JNDI_KEY = "ext.delegateusermgmt.jira.jndi.value";
    public static final String DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY = "ext.delegateusermgmt.isldapauthpresent.value";
    public static final String DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY = "ext.delegateusermgmt.company.ldap.url.value";
    public static final String DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY = "ext.delegateusermgmt.company.ldap.base.dn.value";
    public static final String DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT = "ext.delegateusermgmt.maxuserids.limit.value";
    public static final String DELEGATE_USER_MGMT_PLUGIN_STATUS = "ext.delegateusermgmt.plugin.status";
    public static final String DELEGATE_USER_MGMT_USERGROUPS_MATCHING_PATTERN = "ext.delegateusermgmt.usergroups.matching.pattern";
    public static final String DELEGATE_USER_MGMT_DOWNTIME_MESSAGE = "ext.delegateusermgmt.downtime.message.value";

    public static final String DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE = "CONFLUENCE";
    public static final String DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE = "JIRA";

    public static final String DELEGATE_USER_LDAP_AUTH_KEY_YES_VALUE= "YES";
    public static final String DELEGATE_USER_LDAP_AUTH_KEY_NO_VALUE= "NO";
}
