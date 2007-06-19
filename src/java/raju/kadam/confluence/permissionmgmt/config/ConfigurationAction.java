package raju.kadam.confluence.permissionmgmt.config;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.core.Administrative;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 19, 2007
 * Time: 9:14:23 AM
 */
public class ConfigurationAction  extends ConfluenceActionSupport implements Administrative, CustomPermissionConfigurable {

    private String userManagerLocation;
    private String jiraUrl;
    private String jiraJNDILookupKey;
    private String ldapAuthUsed;
    private String companyLDAPUrl;
    private String companyLDAPBaseDN;
    private String maxUserIDsLimit;
    private String userGroupsMatchingPattern;
    private String pluginDown;
    private String downTimeMessage;


    public String getUserManagerLocation() {
        return userManagerLocation;
    }

    public void setUserManagerLocation(String userManagerLocation) {
        this.userManagerLocation = userManagerLocation;
    }

    public String getJiraUrl() {
        return jiraUrl;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }

    public String getJiraJNDILookupKey() {
        return jiraJNDILookupKey;
    }

    public void setJiraJNDILookupKey(String jiraJNDILookupKey) {
        this.jiraJNDILookupKey = jiraJNDILookupKey;
    }

    public String getLdapAuthUsed() {
        return ldapAuthUsed;
    }

    public void setLdapAuthUsed(String ldapAuthUsed) {
        this.ldapAuthUsed = ldapAuthUsed;
    }

    public String getCompanyLDAPUrl() {
        return companyLDAPUrl;
    }

    public void setCompanyLDAPUrl(String companyLDAPUrl) {
        this.companyLDAPUrl = companyLDAPUrl;
    }

    public String getCompanyLDAPBaseDN() {
        return companyLDAPBaseDN;
    }

    public void setCompanyLDAPBaseDN(String companyLDAPBaseDN) {
        this.companyLDAPBaseDN = companyLDAPBaseDN;
    }

    public String getMaxUserIDsLimit() {
        return maxUserIDsLimit;
    }

    public void setMaxUserIDsLimit(String maxUserIDsLimit) {
        this.maxUserIDsLimit = maxUserIDsLimit;
    }

    public String getUserGroupsMatchingPattern() {
        return userGroupsMatchingPattern;
    }

    public void setUserGroupsMatchingPattern(String userGroupsMatchingPattern) {
        this.userGroupsMatchingPattern = userGroupsMatchingPattern;
    }

    public String getPluginDown() {
        return pluginDown;
    }

    public void setPluginDown(String pluginDown) {
        this.pluginDown = pluginDown;
    }

    public String getDownTimeMessage() {
        return downTimeMessage;
    }

    public void setDownTimeMessage(String downTimeMessage) {
        this.downTimeMessage = downTimeMessage;
    }
}
