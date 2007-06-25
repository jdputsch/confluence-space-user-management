package raju.kadam.confluence.permissionmgmt.config;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.spring.container.ContainerManager;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenience methods that get/set persisted config values in BandanaManager.
 * <p/>
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 2:20:54 PM
 */
public class CustomPermissionConfiguration implements CustomPermissionConfigurable {

    private Log log = LogFactory.getLog(this.getClass());

    private BandanaManager bandanaManager;

    public CustomPermissionConfiguration() {
        log.debug("CustomPermissionConfiguration start constructor");
        setBandanaManager((BandanaManager) ContainerManager.getComponent("bandanaManager"));
        log.debug("CustomPermissionConfiguration end constructor");
    }

    public void copyTo(CustomPermissionConfigurable config) {
        config.setUserManagerLocation(getUserManagerLocation());
        config.setJiraUrl(getJiraUrl());
        config.setJiraJNDILookupKey(getJiraJNDILookupKey());
        config.setMaxUserIDsLimit(getMaxUserIDsLimit());
        config.setUserGroupsMatchingPattern(getUserGroupsMatchingPattern());
        config.setLdapAuthUsed(getLdapAuthUsed());
        config.setCompanyLDAPUrl(getCompanyLDAPUrl());
        config.setCompanyLDAPBaseDN(getCompanyLDAPBaseDN());
        config.setPluginDown(getPluginDown());
        config.setDownTimeMessage(getDownTimeMessage());
        config.setGroupActionsPermitted(getGroupActionsPermitted());
        config.setNewGroupNameCreationPrefixPattern(getNewGroupNameCreationPrefixPattern());
        config.setNewGroupNameCreationSuffixPattern(getNewGroupNameCreationSuffixPattern());
    }

    public void updateWith(CustomPermissionConfigurable config) {
        setUserManagerLocation(config.getUserManagerLocation());
        setJiraUrl(config.getJiraUrl());
        setJiraJNDILookupKey(config.getJiraJNDILookupKey());
        setMaxUserIDsLimit(config.getMaxUserIDsLimit());
        setUserGroupsMatchingPattern(config.getUserGroupsMatchingPattern());
        setLdapAuthUsed(config.getLdapAuthUsed());
        setCompanyLDAPUrl(config.getCompanyLDAPUrl());
        setCompanyLDAPBaseDN(config.getCompanyLDAPBaseDN());
        setPluginDown(config.getPluginDown());
        setDownTimeMessage(config.getDownTimeMessage());
        setGroupActionsPermitted(config.getGroupActionsPermitted());
        setNewGroupNameCreationPrefixPattern(config.getNewGroupNameCreationPrefixPattern());
        setNewGroupNameCreationSuffixPattern(config.getNewGroupNameCreationSuffixPattern());
    }

    public boolean isValid() {
        boolean isValid = true;

        String userManagementLocation = getUserManagerLocation();
        if (userManagementLocation == null) {
            isValid = false;
        }
        else {
            if (userManagementLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE)) {
                //if user is using JIRA for User Management then we need to Jira Information
                if ((getJiraUrl() == null) || (getJiraJNDILookupKey() == null)) {
                    isValid = false;
                }
            }
        }

        if (getMaxUserIDsLimit() == null || !isIntGreaterThanZero(getMaxUserIDsLimit())) {
            isValid = false;
        }

        String userGroupsMatchingPattern = getUserGroupsMatchingPattern();
        if (getUserGroupsMatchingPattern() == null) {
            isValid = false;
        }
        else {
            try {
                //validate is valid Pattern
                Pattern pat = Pattern.compile(userGroupsMatchingPattern);
            }
            catch (PatternSyntaxException pse) {
                isValid = false;
            }
        }

        String ldapAuthUsed = getLdapAuthUsed();
        if (ldapAuthUsed==null) {
            isValid = false;
        }
        else {
            if (isNotNullAndIsYesOrNo(ldapAuthUsed)) {
                if ("YES".equals(ldapAuthUsed)) {
                    if (getCompanyLDAPUrl() == null || getCompanyLDAPBaseDN() == null) {
                        isValid = false;
                    }
                }
            }
            else {
                isValid = false;
            }
        }

        String pluginInDown = getPluginDown();
        if (isNotNullAndIsYesOrNo(pluginInDown)) {
            if ("YES".equals(pluginInDown)) {
                if (getDownTimeMessage() == null) {
                    isValid = false;
                }
            }
        }
        else {
            isValid = false;
        }

        String groupActionsPermitted = getGroupActionsPermitted();
        if (isNotNullAndIsYesOrNo(groupActionsPermitted)) {
            if ("YES".equals(groupActionsPermitted)) {
                if (getNewGroupNameCreationPrefixPattern() == null || getNewGroupNameCreationSuffixPattern() == null) {
                    isValid = false;
                }
            }
        }
        else {
            isValid = false;
        }

        return isValid;
    }

    private boolean isNotNullAndIsYesOrNo(String s) {
        boolean result = false;
        if ( s != null && ("YES".equals(s) || "NO".equals(s))) {
            result = true;
        }
        return result;
    }

    private boolean isIntGreaterThanZero(String s) {
        boolean result = false;
        if ( s != null ) {
            int i = 0;
            try {
                i = Integer.parseInt(s);
                if (i>0) {
                    result = true;
                }
            }
            catch (NumberFormatException nfe) {
                // invalid
            }
        }
        return result;
    }

    public String getUserManagerLocation() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_USER_MANAGER_LOCATION);
    }

    public void setUserManagerLocation(String userManagerLocation) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_USER_MANAGER_LOCATION, userManagerLocation);
    }

    public String getJiraUrl() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_URL);
    }

    public void setJiraUrl(String jiraUrl) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_URL, jiraUrl);
    }

    public String getJiraJNDILookupKey() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_JNDI_KEY);
    }

    public void setJiraJNDILookupKey(String jiraJNDILookupKey) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_JNDI_KEY, jiraJNDILookupKey);
    }

    public String getLdapAuthUsed() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY);
    }

    public void setLdapAuthUsed(String ldapAuthUsed) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY, ldapAuthUsed);
    }

    public String getCompanyLDAPUrl() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY);
    }

    public void setCompanyLDAPUrl(String companyLDAPUrl) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY, companyLDAPUrl);
    }

    public String getCompanyLDAPBaseDN() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY);
    }

    public void setCompanyLDAPBaseDN(String companyLDAPBaseDN) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY, companyLDAPBaseDN);
    }

    public String getMaxUserIDsLimit() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT);
    }

    public void setMaxUserIDsLimit(String maxUserIDsLimit) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT, maxUserIDsLimit);
    }

    public String getUserGroupsMatchingPattern() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_USERGROUPS_MATCHING_PATTERN);
    }

    public void setUserGroupsMatchingPattern(String userGroupsMatchingPattern) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_USERGROUPS_MATCHING_PATTERN, userGroupsMatchingPattern);
    }

    public String getPluginDown() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_PLUGIN_STATUS);
    }

    public void setPluginDown(String pluginDown) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_PLUGIN_STATUS, pluginDown);
    }

    public String getDownTimeMessage() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_DOWNTIME_MESSAGE);
    }

    public void setDownTimeMessage(String downTimeMessage) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_DOWNTIME_MESSAGE, downTimeMessage);
    }

    public String getGroupActionsPermitted() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_GROUP_ACTIONS_PERMITTED);
    }

    public void setGroupActionsPermitted(String groupActionsPermitted) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_GROUP_ACTIONS_PERMITTED, groupActionsPermitted);
    }

    public String getNewGroupNameCreationPrefixPattern() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_PREFIX_PATTERN);
    }

    public void setNewGroupNameCreationPrefixPattern(String newGroupNameCreationPrefixPattern) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_PREFIX_PATTERN, newGroupNameCreationPrefixPattern);
    }

    public String getNewGroupNameCreationSuffixPattern() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_SUFFIX_PATTERN);
    }

    public void setNewGroupNameCreationSuffixPattern(String newGroupNameCreationSuffixPattern) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_SUFFIX_PATTERN, newGroupNameCreationSuffixPattern);
    }

    public BandanaManager getBandanaManager() {
        return bandanaManager;
    }

    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }
}
