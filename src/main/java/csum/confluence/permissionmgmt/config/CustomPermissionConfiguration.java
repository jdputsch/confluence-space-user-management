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

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.spring.container.ContainerManager;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import csum.confluence.permissionmgmt.util.ConfigUtil;
import csum.confluence.permissionmgmt.util.group.GroupNameUtil;
import csum.confluence.permissionmgmt.AbstractPagerPaginationSupportCachingSpaceAction;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapServiceServiceLocator;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapService;

/**
 * @author Gary S. Weaver
 */
public class CustomPermissionConfiguration implements CustomPermissionConfigurable {

    // has a static method, so need to use static log
    private static final Log log = LogFactory.getLog(CustomPermissionConfiguration.class);

    private BandanaManager bandanaManager;

    public CustomPermissionConfiguration() {
        log.debug("CustomPermissionConfiguration start constructor");
        setBandanaManager((BandanaManager) ContainerManager.getComponent("bandanaManager"));
        log.debug("CustomPermissionConfiguration end constructor");
    }

    public void copyTo(CustomPermissionConfigurable config) {
        config.setUserManagerLocation(getUserManagerLocation());
        config.setMaxUserIDsLimit(getMaxUserIDsLimit());
        config.setMaxGroupIDsLimit(getMaxGroupIDsLimit());
        config.setUserGroupsMatchingPattern(getUserGroupsMatchingPattern());
        config.setLdapAuthUsed(getLdapAuthUsed());
        config.setPluginDown(getPluginDown());
        config.setDownTimeMessage(getDownTimeMessage());
        config.setGroupActionsPermitted(getGroupActionsPermitted());
        config.setNewGroupNameCreationPrefixPattern(getNewGroupNameCreationPrefixPattern());
        config.setNewGroupNameCreationSuffixPattern(getNewGroupNameCreationSuffixPattern());
        config.setUserSearchEnabled(getUserSearchEnabled());
        config.setJiraSoapUrl(getJiraSoapUrl());
        config.setJiraSoapUsername(getJiraSoapUsername());
        config.setJiraSoapPassword(getJiraSoapPassword());
    }

    public void updateWith(CustomPermissionConfigurable config) {
        setUserManagerLocation(config.getUserManagerLocation());
        setMaxUserIDsLimit(config.getMaxUserIDsLimit());
        setMaxGroupIDsLimit(config.getMaxGroupIDsLimit());
        setUserGroupsMatchingPattern(config.getUserGroupsMatchingPattern());
        setLdapAuthUsed(config.getLdapAuthUsed());
        setPluginDown(config.getPluginDown());
        setDownTimeMessage(config.getDownTimeMessage());
        setGroupActionsPermitted(config.getGroupActionsPermitted());
        setNewGroupNameCreationPrefixPattern(config.getNewGroupNameCreationPrefixPattern());
        setNewGroupNameCreationSuffixPattern(config.getNewGroupNameCreationSuffixPattern());
        setUserSearchEnabled(config.getUserSearchEnabled());
        setJiraSoapUrl(config.getJiraSoapUrl());
        setJiraSoapUsername(config.getJiraSoapUsername());

        String jiraSoapPassword = getJiraSoapPassword();
        if (jiraSoapPassword!=null) {
            // only change password if it is set to a string (can be empty, but if null that indicates not to change)
            config.setJiraSoapPassword(jiraSoapPassword);
        }

        // config has changed. clear ALL cache including indexes!!!
        AbstractPagerPaginationSupportCachingSpaceAction.clearCacheIncludingIndexes();
    }

    /**
     * Validatation for configuration from manager area instead of config area. Otherwise, use
     * validate(config,existingConfig).
     *
     * @return
     */
    public ConfigValidationResponse validate() {
        // this is kind of quirky-looking because the validate(config,existingConfig) method is able to use this
        // existing password for JIRA SOAP testing when entering config from config UI
        return validate(this, this);
    }

    public static ConfigValidationResponse validate(CustomPermissionConfigurable config, CustomPermissionConfigurable existingConfig) {

        ConfigValidationResponse result = new ConfigValidationResponse();
        result.setValid(true);

        boolean isUserManagerLocationSet = true;
		String userMgrLocation = config.getUserManagerLocation();
		boolean userManagerLocationIsConfluence = (userMgrLocation != null) && (userMgrLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE));
		boolean userManagerLocationIsJira = (userMgrLocation != null) && (userMgrLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE));

		//If userManagerLocation is not set as CONFLUENCE or JIRA, then it must be set to either value.
		if( !(userManagerLocationIsConfluence || userManagerLocationIsJira) )
		{
            result.addFieldError("userManagerLocation", "Please indicate which application manages Wiki Users");
            result.setValid(false);
		}

		//Following information needs to be check only if Wiki User Management is delegated to Jira
		if(isUserManagerLocationSet && userManagerLocationIsJira)
		{
			if (ConfigUtil.isNullOrEmpty(config.getJiraSoapUrl())) {
                result.addFieldError("jiraSoapUrl", "JIRA SOAP URL cannot be empty");
                result.setValid(false);
            }

            if (ConfigUtil.isNullOrEmpty(config.getJiraSoapUsername())) {
                result.addFieldError("jiraSoapUsername", "JIRA SOAP username cannot be empty");
                result.setValid(false);
            }

            // test connection
            try {
                JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
                jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(config.getJiraSoapUrl());
                JiraSoapService jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();

                String passwd = config.getJiraSoapPassword();
                if (passwd==null) {
                    //chose not to set password so get existing password
                    passwd = existingConfig.getJiraSoapPassword();
                }

                // Note: as long as we are ONLY logging in and logging out, don't need to save token and logout in finally
                String token = jiraSoapService.login(config.getJiraSoapUsername(), passwd);
                jiraSoapService.logout(token);
            }
            catch (Throwable t) {
                log.error("Problem testing JIRA SOAP configuration by connecting to JIRA", t);
                result.addFieldError("jiraSoapUrl", "Problem logging in to JIRA SOAP service: " + t);
                result.setValid(false);
            }
        }

        if (!ConfigUtil.isNotNullAndIsYesOrNo(config.getLdapAuthUsed())) {
            result.addFieldError("ldapAuthUsed", "Must be YES or NO");
            result.setValid(false);
        }        		

        if (!ConfigUtil.isNotNullAndIsYesOrNo(config.getUserSearchEnabled())) {
            result.addFieldError("userSearchEnabled", "Must be YES or NO");
            result.setValid(false);
        }

        if (config.getMaxUserIDsLimit() == null || !ConfigUtil.isIntGreaterThanZero(config.getMaxUserIDsLimit())) {
            result.addFieldError("maxUserIDsLimit", "Can only be empty or an integer greater than zero");
            result.setValid(false);
        }

        if (config.getMaxGroupIDsLimit() == null || !ConfigUtil.isIntGreaterThanZero(config.getMaxGroupIDsLimit())) {
            result.addFieldError("maxGroupIDsLimit", "Can only be empty or an integer greater than zero");
            result.setValid(false);
        }

        String userGroupsMatchingPattern = config.getUserGroupsMatchingPattern();
        if (ConfigUtil.isNullOrEmpty(userGroupsMatchingPattern)) {
            result.addFieldError("userGroupsMatchingPattern", "Group matching pattern cannot be null or empty");
            result.setValid(false);
        }
        else {
            try {
                //validate some patterns
                //note: Space keys may only consist of ASCII letters or numbers (A-Z, a-z, 0-9) up to 255 chars
                validatePattern(result, config, "tst", getValidationGroupName(config, "tst", "spongebob"));
                validatePattern(result, config, "tst", getValidationGroupName(config, "tst", "spongebob").toUpperCase());
                validatePattern(result, config, "tst", getValidationGroupName(config, "tst", "spongebob").toLowerCase());
                validatePattern(result, config, "Demo10", getValidationGroupName(config, "Demo10", "Best-Group-Name-In-The-World"));
                validatePattern(result, config, "Demo10", getValidationGroupName(config, "Demo10", "Best-Group-Name-In-The-World").toUpperCase());
                validatePattern(result, config, "Demo10", getValidationGroupName(config, "Demo10", "Best-Group-Name-In-The-World").toLowerCase());
                validatePattern(result, config, "ILoveBass", getValidationGroupName(config, "ILoveBass", "realbasslovers"));
                validatePattern(result, config, "ILoveBass", getValidationGroupName(config, "ILoveBass", "realbasslovers").toUpperCase());
                validatePattern(result, config, "ILoveBass", getValidationGroupName(config, "ILoveBass", "realbasslovers").toLowerCase());
                validatePattern(result, config, "DS", getValidationGroupName(config, "DS", "FooBar"));
                validatePattern(result, config, "DS", getValidationGroupName(config, "DS", "FooBar").toUpperCase());
                validatePattern(result, config, "DS", getValidationGroupName(config, "DS", "FooBar").toLowerCase());                
            }
            catch (PatternSyntaxException pse) {
                result.addFieldError("userGroupsMatchingPattern", "Group matching pattern was invalid: " + pse.getMessage());
                result.setValid(false);
            }
        }

        String pluginInDown = config.getPluginDown();
        if (ConfigUtil.isNotNullAndIsYesOrNo(pluginInDown)) {
            if ("YES".equals(pluginInDown)) {
                // is ok to be empty
                if (config.getDownTimeMessage() == null) {
                    result.addFieldError("pluginInDown", "Downtime message must be specified if plugin is down");
                    result.setValid(false);
                }
            }
        }
        else {
            result.addFieldError("pluginInDown", "Must be YES or NO");
            result.setValid(false);
        }

        String groupActionsPermitted = config.getGroupActionsPermitted();
        if (ConfigUtil.isNotNullAndIsYesOrNo(groupActionsPermitted)) {
            if ("YES".equals(groupActionsPermitted)) {
                // these are ok to be empty
                if (config.getNewGroupNameCreationPrefixPattern() == null || config.getNewGroupNameCreationSuffixPattern() == null) {
                    result.addFieldError("groupActionsPermitted", "If group actions permitted, must specify prefix and suffix (even if they are just empty). They should fit with the group name pattern specified.");
                    result.setValid(false);
                }
            }
        }
        else {
            result.addFieldError("groupActionsPermitted", "Must be YES or NO");
            result.setValid(false);
        }

        log.debug("CustomPermissionConfigAction - isValid=" + result + " fieldErrors=" + result.getFieldNameToErrorMessage());
        return result;
    }

    private static String getValidationGroupName(CustomPermissionConfigurable config, String aSpaceKey, String aGroupMiddle) {
        String replacedPrefix = GroupNameUtil.replaceSpaceKey(config.getNewGroupNameCreationPrefixPattern(), aSpaceKey);
        String replacedSuffix = GroupNameUtil.replaceSpaceKey(config.getNewGroupNameCreationSuffixPattern(), aSpaceKey);
        String aGroupname = replacedPrefix + aGroupMiddle + replacedSuffix;
        return aGroupname;
    }

    private static void validatePattern(ConfigValidationResponse result, CustomPermissionConfigurable config,
                                        String aSpaceKey, String aGroupname) {
        Pattern pattern = GroupNameUtil.createGroupMatchingPattern(config, aSpaceKey);
        String groupsActionsPermitted = config.getGroupActionsPermitted();
        if ( groupsActionsPermitted!= null && CustomPermissionConfigConstants.YES.equals(groupsActionsPermitted) ) {
            if (!GroupNameUtil.doesGroupMatchPattern( aGroupname, pattern )) {
                result.addFieldError("userGroupsMatchingPattern", "Based on current prefix and suffix, if spacekey were " +
                        aSpaceKey +
                        " this pattern should match groupname " + aGroupname.toLowerCase() + " but it did not. " +
                        "Please modify the pattern so that it works or visit the " +
                        "<a href=\"http://confluence.atlassian.com/display/CONFEXT/Custom+Space+User+Management+Plugin\">" +
                        "plugin homepage</a> for support.");
                result.setValid(false);
            }
        }
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

    public String getMaxGroupIDsLimit() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_MAXGROUPIDS_LIMIT);
    }

    public void setMaxGroupIDsLimit(String maxGroupIDsLimit) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_MAXGROUPIDS_LIMIT, maxGroupIDsLimit);
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

    public String getUserSearchEnabled() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_USER_SEARCH_ENABLED);
    }

    public void setUserSearchEnabled(String userSearchEnabled) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_USER_SEARCH_ENABLED, userSearchEnabled);
    }

    public String getJiraSoapUrl() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_SOAP_URL);
    }

    public void setJiraSoapUrl(String jiraSoapUrl) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_SOAP_URL, jiraSoapUrl);
    }

    public String getJiraSoapUsername() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_SOAP_USERNAME);
    }

    public void setJiraSoapUsername(String jiraSoapUsername) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_SOAP_USERNAME, jiraSoapUsername);
    }

    public String getJiraSoapPassword() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_SOAP_PASSWORD);
    }

    public void setJiraSoapPassword(String jiraSoapPassword) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_SOAP_PASSWORD, jiraSoapPassword);
    }

    public BandanaManager getBandanaManager() {
        return bandanaManager;
    }

    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }
}
