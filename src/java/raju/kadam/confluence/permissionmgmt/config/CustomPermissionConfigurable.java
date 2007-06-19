package raju.kadam.confluence.permissionmgmt.config;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 19, 2007
 * Time: 9:08:32 AM
 */
public interface CustomPermissionConfigurable {

    public String getUserManagerLocation();
    public void setUserManagerLocation(String userManagerLocation);

    public String getJiraUrl();
    public void setJiraUrl(String jiraUrl);

    public String getJiraJNDILookupKey();
    public void setJiraJNDILookupKey(String jiraJNDILookupKey);

    public String getLdapAuthUsed();
    public void setLdapAuthUsed(String ldapAuthUsed);

    public String getCompanyLDAPUrl();
    public void setCompanyLDAPUrl(String companyLDAPUrl);

    public String getCompanyLDAPBaseDN();
    public void setCompanyLDAPBaseDN(String companyLDAPBaseDN);

    public String getMaxUserIDsLimit();
    public void setMaxUserIDsLimit(String maxUserIDsLimit);

    public String getUserGroupsMatchingPattern();
    public void setUserGroupsMatchingPattern(String userGroupsMatchingPattern);

    public String getPluginDown();
    public void setPluginDown(String pluginDown);
    
    public String getDownTimeMessage();
    public void setDownTimeMessage(String downTimeMessage);
}
