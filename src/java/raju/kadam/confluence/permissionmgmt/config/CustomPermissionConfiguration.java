package raju.kadam.confluence.permissionmgmt.config;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.spring.container.ContainerManager;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import raju.kadam.util.ConfigUtil;
import raju.kadam.util.StringUtil;
import raju.kadam.confluence.permissionmgmt.util.JiraUtil;

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
            log.warn("userManagementLocation was null");
            isValid = false;
        }
        else {
            if ( userManagementLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE) ) {

                if ( ConfigUtil.isNullOrEmpty(getJiraJNDILookupKey())) {
                    log.warn("jiraJNDILookupKey was null/empty");
                    isValid = false;
                }
                else {
                    try {
                        if (ConfigUtil.isNullOrEmpty(JiraUtil.getJiraSoapUrl()) ||
                            ConfigUtil.isNullOrEmpty(JiraUtil.getJiraSoapUsername()) ||
                            ConfigUtil.isNullOrEmpty(JiraUtil.getJiraSoapPassword()))
                        {
                            log.warn("jira properties were null/empty");
                            isValid = false;
                        }
                    }
                    catch (Throwable t) {
                        log.error("Got error while trying to check values in properties file!", t);
                        isValid = false;
                    }
                }
            }
        }

        if (getMaxUserIDsLimit() == null || !ConfigUtil.isIntGreaterThanZero(getMaxUserIDsLimit())) {
            log.warn("maxUserIDsLimit was invalid");
            isValid = false;
        }

        String userGroupsMatchingPattern = getUserGroupsMatchingPattern();
        if (ConfigUtil.isNullOrEmpty(getUserGroupsMatchingPattern())) {
            log.warn("matching pattern was null/empty");
            isValid = false;
        }
        else {
            try {
                //validate is valid Pattern
                Pattern.compile(userGroupsMatchingPattern);
            }
            catch (PatternSyntaxException pse) {
                log.error("Pattern specified in config failed to compile", pse);
                isValid = false;
            }
        }

        String ldapAuthUsed = getLdapAuthUsed();
        if (ConfigUtil.isNotNullAndIsYesOrNo(ldapAuthUsed)) {
            if ("YES".equals(ldapAuthUsed)) {
                if (ConfigUtil.isNullOrEmpty(getCompanyLDAPUrl()) || getCompanyLDAPBaseDN() == null) {
                    log.warn("ldap URL was null/empty or LDAP base DN was null");
                    isValid = false;
                }
            }
        }
        else {
            log.warn("ldapAuthUsed was not YES or NO");
            isValid = false;
        }

        String pluginInDown = getPluginDown();
        if (ConfigUtil.isNotNullAndIsYesOrNo(pluginInDown)) {
            if ("YES".equals(pluginInDown)) {
                // is ok to be empty
                if (getDownTimeMessage() == null) {
                    log.warn("plugin was down but no downtime message specified");
                    isValid = false;
                }
            }
        }
        else {
            log.warn("isPluginDown was not YES or NO");
            isValid = false;
        }

        String groupActionsPermitted = getGroupActionsPermitted();
        if (ConfigUtil.isNotNullAndIsYesOrNo(groupActionsPermitted)) {
            if ("YES".equals(groupActionsPermitted)) {
                // these are ok to be empty
                if (getNewGroupNameCreationPrefixPattern() == null || getNewGroupNameCreationSuffixPattern() == null) {
                    log.warn("new group name prefix or suffix was null");
                    isValid = false;
                }
            }
        }
        else {
            log.warn("group actions permitted was not YES or NO");
            isValid = false;
        }

        return isValid;
    }

    public String getUserManagerLocation() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_USER_MANAGER_LOCATION);
    }

    public void setUserManagerLocation(String userManagerLocation) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_USER_MANAGER_LOCATION, userManagerLocation);
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
