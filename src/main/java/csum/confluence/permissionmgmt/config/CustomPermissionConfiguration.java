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
import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.spring.container.ContainerManager;
import csum.confluence.permissionmgmt.AbstractPagerPaginationSupportCachingSpaceAction;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapService;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapServiceServiceLocator;
import csum.confluence.permissionmgmt.util.ConfigUtil;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.ldap.LDAPHelper;
import com.dolby.confluence.net.ldap.LDAPUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.StringWriter;
import java.io.PrintWriter;

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
        config.setProviderType(getProviderType());
        config.setLdapUserIdAttribute(getLdapUserIdAttribute());
        config.setLdapEmailAttribute(getLdapEmailAttribute());
        config.setLdapFirstNameAttribute(getLdapFirstNameAttribute());
        config.setLdapLastNameAttribute(getLdapLastNameAttribute());
        config.setLdapProviderFullyQualifiedClassname(getLdapProviderFullyQualifiedClassname());
        config.setUserFullNameFormat(getUserFullNameFormat());
        config.setLdapConfigTestUsername(getLdapConfigTestUsername());
        config.setLdapNarrowingFilterExpression(getLdapNarrowingFilterExpression());
        config.setPersonalSpaceAllowed(getPersonalSpaceAllowed());
    }

    public void updateWith(CustomPermissionConfigurable config) {
        setUserManagerLocation(config.getUserManagerLocation());
        setMaxUserIDsLimit(config.getMaxUserIDsLimit());
        setMaxGroupIDsLimit(config.getMaxGroupIDsLimit());
        setLdapAuthUsed(config.getLdapAuthUsed());
        setPluginDown(config.getPluginDown());
        setDownTimeMessage(config.getDownTimeMessage());
        setGroupActionsPermitted(config.getGroupActionsPermitted());
        setNewGroupNameCreationPrefixPattern(config.getNewGroupNameCreationPrefixPattern());
        setNewGroupNameCreationSuffixPattern(config.getNewGroupNameCreationSuffixPattern());
        setUserSearchEnabled(config.getUserSearchEnabled());
        setJiraSoapUrl(config.getJiraSoapUrl());
        setJiraSoapUsername(config.getJiraSoapUsername());

        String jiraSoapPassword = config.getJiraSoapPassword();
        if (jiraSoapPassword!=null) {
            // only change password if it is set to a string (can be empty, but if null that indicates not to change)
            setJiraSoapPassword(jiraSoapPassword);
        }

        setProviderType(config.getProviderType());
        setLdapUserIdAttribute(config.getLdapUserIdAttribute());
        setLdapEmailAttribute(config.getLdapEmailAttribute());
        setLdapFirstNameAttribute(config.getLdapFirstNameAttribute());
        setLdapLastNameAttribute(config.getLdapLastNameAttribute());
        setLdapProviderFullyQualifiedClassname(config.getLdapProviderFullyQualifiedClassname());
        setUserFullNameFormat(config.getUserFullNameFormat());
        setLdapConfigTestUsername(config.getLdapConfigTestUsername());
        setLdapNarrowingFilterExpression(config.getLdapNarrowingFilterExpression());
        setPersonalSpaceAllowed(config.getPersonalSpaceAllowed());

        // config has changed. clear ALL cache including indexes!!!
        AbstractPagerPaginationSupportCachingSpaceAction.clearCacheIncludingIndexes();
    }

    /**
     * Validatation for configuration from manager area instead of config area. Otherwise, use
     * validate(config,existingConfig).
     *
     * @return ConfigValidationResponse
     */
    public ConfigValidationResponse validate(ConfluenceActionSupport cas) {
        return validate(this, null, null, cas);
    }

    public static ConfigValidationResponse validate(CustomPermissionConfigurable config, CustomPermissionConfigurable existingConfig, String remoteUser, ConfluenceActionSupport cas) {

        ConfigValidationResponse result = new ConfigValidationResponse();
        result.setValid(true);
        
		String userMgrLocation = config.getUserManagerLocation();
		boolean isUserManagerLocationSet = (userMgrLocation != null);
        boolean userManagerLocationIsConfluence = (userMgrLocation != null) && (userMgrLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE));
		boolean userManagerLocationIsJira = (userMgrLocation != null) && (userMgrLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE));

		//If userManagerLocation is not set as CONFLUENCE or JIRA, then it must be set to either value.
		if( !(userManagerLocationIsConfluence || userManagerLocationIsJira) )
		{
            result.addFieldError("userManagerLocation", cas.getText("configure.error.usermanagerlocationnull"));
            result.setValid(false);
		}

		//Following information needs to be check only if Wiki User Management is delegated to Jira
		if(isUserManagerLocationSet && userManagerLocationIsJira)
		{
			boolean testSoapService = true;
            if (ConfigUtil.isNullOrEmpty(config.getJiraSoapUrl())) {
                testSoapService = false;
                result.addFieldError("jiraSoapUrl", cas.getText("configure.error.jirasoapurlempty"));
                result.setValid(false);
            }

            if (ConfigUtil.isNullOrEmpty(config.getJiraSoapUsername())) {
                testSoapService = false;
                result.addFieldError("jiraSoapUsername", cas.getText("configure.error.jirasoapusernameempty"));
                result.setValid(false);
            }

            String jiraSoapPassword = null;
            if (config.getJiraSoapPassword()!=null) {
                jiraSoapPassword = config.getJiraSoapPassword();
            }
            else if (existingConfig != null && existingConfig.getJiraSoapPassword()!=null) {
                jiraSoapPassword = existingConfig.getJiraSoapPassword();
            }
            else {
                testSoapService = false;
                result.addFieldError("jiraSoapPassword", cas.getText("configure.error.jirasoappasswordnull"));
                result.setValid(false);
            }

            // test connection
            if (testSoapService) {
                try {
                    JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
                    jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(config.getJiraSoapUrl());
                    JiraSoapService jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();

                    // Note: as long as we are ONLY logging in and logging out, don't need to save token and logout in finally
                    String token = jiraSoapService.login(config.getJiraSoapUsername(), jiraSoapPassword);
                    jiraSoapService.logout(token);
                }
                catch (Throwable t) {
                    log.error("Problem testing JIRA SOAP configuration by connecting to JIRA", t);
                    result.addFieldError("jiraSoapUrl", cas.getText("configure.error.jirasoaptestconnectfailed") + ": " + t);
                    result.setValid(false);
                }
            }
        }

        if (!ConfigUtil.isNotNullAndIsYesOrNo(config.getLdapAuthUsed())) {
            result.addFieldError("ldapAuthUsed", cas.getText("configure.error.ldapauthusedinvalid"));
            result.setValid(false);
        }
        else {
            if ("YES".equals(config.getLdapAuthUsed())) {
                String providerType = config.getProviderType();
                if( providerType == null ||
                        (!providerType.equals(CustomPermissionConfigConstants.PROVIDER_TYPE_OSUSER) &&
                       !providerType.equals(CustomPermissionConfigConstants.PROVIDER_TYPE_ATLASSIAN_USER)))
                {
                    result.addFieldError("providerType", cas.getText("configure.error.providertypeinvalid"));
                    result.setValid(false);
                }

                if (config.getLdapUserIdAttribute()==null) {
                    result.addFieldError("ldapUserIdAttribute", cas.getText("configure.error.ldapuseridattributenull"));
                    result.setValid(false);
                }

                if (config.getLdapEmailAttribute()==null) {
                    result.addFieldError("ldapEmailAttribute", cas.getText("configure.error.ldapemailattributenull"));
                    result.setValid(false);
                }

                if (config.getLdapFirstNameAttribute()==null) {
                    result.addFieldError("ldapFirstNameAttribute", cas.getText("configure.error.ldapfirstnameattributenull"));
                    result.setValid(false);
                }

                if (config.getLdapLastNameAttribute()==null) {
                    result.addFieldError("ldapLastNameAttribute", cas.getText("configure.error.ldaplastnameattributenull"));
                    result.setValid(false);
                }

                String userFullNameFormat = config.getUserFullNameFormat();
                boolean lastCommaFirstFormat = (userFullNameFormat != null) && (userFullNameFormat.equals(CustomPermissionConfigConstants.USER_FULL_NAME_FORMAT_TYPE_LASTNAME_COMMA_FIRSTNAME));
                boolean firstLastFormat = (userFullNameFormat != null) && (userFullNameFormat.equals(CustomPermissionConfigConstants.USER_FULL_NAME_FORMAT_TYPE_FIRSTNAME_LASTNAME));
                boolean idFormat = (userFullNameFormat != null) && (userFullNameFormat.equals(CustomPermissionConfigConstants.USER_FULL_NAME_FORMAT_TYPE_ID));

                if (userFullNameFormat==null) {
                    result.addFieldError("userFullNameFormat", cas.getText("configure.error.userfullnameformatnull"));
                    result.setValid(false);
                }
                else {
                    if (!lastCommaFirstFormat && !firstLastFormat && !idFormat) {
                        result.addFieldError("userFullNameFormat", cas.getText("configure.error.userfullnameformatinvalid") + ": " + userFullNameFormat);
                        result.setValid(false);
                    }
                }

                if (config.getLdapProviderFullyQualifiedClassname()==null) {
                    // ok to be empty. is not used in user-atlassian provider implementation
                    result.addFieldError("ldapProviderFullyQualifiedClassname", cas.getText("configure.error.ldapproviderfullyqualifiedclassnamenull"));
                    result.setValid(false);
                }

                String ldapConfigTestUsername = config.getLdapConfigTestUsername();
                if (StringUtil.isNullOrEmpty(ldapConfigTestUsername)) {
                    result.addFieldError("ldapConfigTestUsername", cas.getText("configure.error.ldapconfigtestusernameempty"));
                    result.setValid(false);
                }

                // narrowing filter expression is configurable but is optional and don't even care if it is null or empty

                if (result.isValid())
                {
                    try {
                        LDAPUser usr = LDAPHelper.getLDAPUser(config, ldapConfigTestUsername);
                        if(usr == null)
                        {
                            log.debug("Got null user back from LDAP for " + ldapConfigTestUsername);
                            result.addFieldError("ldapAuthUsed", cas.getText("configure.error.ldapconfigtestreturnednull") + ": " + ldapConfigTestUsername);
                            result.setValid(false);
                        }
                    }
                    catch (Throwable t) {
                        log.error("Problem testing LDAP config in config UI", t);
                        result.addFieldError("ldapAuthUsed", cas.getText("configure.error.ldapconfigtestfailure") + ": " + t.getMessage());
                        result.setValid(false);
                    }
                }
            }
        }

        if (!ConfigUtil.isNotNullAndIsYesOrNo(config.getUserSearchEnabled())) {
            result.addFieldError("userSearchEnabled", cas.getText("configure.error.usersearchenabledinvalid"));
            result.setValid(false);
        }

        if (config.getMaxUserIDsLimit() == null || !ConfigUtil.isIntGreaterThanZero(config.getMaxUserIDsLimit())) {
            result.addFieldError("maxUserIDsLimit", cas.getText("configure.error.maxuseridslimitinvalid"));
            result.setValid(false);
        }

        if (config.getMaxGroupIDsLimit() == null || !ConfigUtil.isIntGreaterThanZero(config.getMaxGroupIDsLimit())) {
            result.addFieldError("maxGroupIDsLimit", cas.getText("configure.error.maxgroupidslimitinvalid"));
            result.setValid(false);
        }

        String pluginInDown = config.getPluginDown();
        if (ConfigUtil.isNotNullAndIsYesOrNo(pluginInDown)) {
            if ("YES".equals(pluginInDown)) {
                // is ok to be empty
                if (config.getDownTimeMessage() == null) {
                    result.addFieldError("pluginInDown", cas.getText("configure.error.downtimemessagenull"));
                    result.setValid(false);
                }
            }
        }
        else {
            result.addFieldError("pluginInDown", cas.getText("configure.error.plugindowninvalid"));
            result.setValid(false);
        }

        String groupActionsPermitted = config.getGroupActionsPermitted();
        if (ConfigUtil.isNotNullAndIsYesOrNo(groupActionsPermitted)) {
            if ("YES".equals(groupActionsPermitted)) {
                // these are ok to be empty
                if (config.getNewGroupNameCreationPrefixPattern() == null) {
                    result.addFieldError("groupActionsPermitted", cas.getText("configure.error.newgroupnamecreationprefixpatterninvalid"));
                    result.setValid(false);
                }

                if (config.getNewGroupNameCreationSuffixPattern() == null) {
                    result.addFieldError("groupActionsPermitted", cas.getText("configure.error.newgroupnamecreationsuffixpatterninvalid"));
                    result.setValid(false);
                }
            }
        }
        else {
            result.addFieldError("groupActionsPermitted", cas.getText("configure.error.groupactionspermittedinvalid"));
            result.setValid(false);
        }

        if (!ConfigUtil.isNotNullAndIsYesOrNo(config.getPersonalSpaceAllowed())) {
            result.addFieldError("personalSpaceAllowed", cas.getText("configure.error.personalspaceallowedinvalid"));
            result.setValid(false);
        }

        log.debug("CustomPermissionConfigAction - isValid=" + result + " fieldErrors=" + result.getFieldNameToErrorMessage());
        return result;
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

    public String getProviderType() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_PROVIDER_TYPE);
    }

    public void setProviderType(String providerType) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_PROVIDER_TYPE, providerType);
    }

    public String getLdapUserIdAttribute() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_USER_ID_ATTRIBUTE);
    }

    public void setLdapUserIdAttribute(String ldapUserIdAttribute) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_USER_ID_ATTRIBUTE, ldapUserIdAttribute);
    }

    public String getLdapEmailAttribute() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_EMAIL_ATTRIBUTE);
    }

    public void setLdapEmailAttribute(String ldapEmailAttribute) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_EMAIL_ATTRIBUTE, ldapEmailAttribute);
    }

    public String getLdapFirstNameAttribute() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_FIRSTNAME_ATTRIBUTE);
    }

    public void setLdapFirstNameAttribute(String ldapNameAttribute) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_FIRSTNAME_ATTRIBUTE, ldapNameAttribute);
    }

    public String getLdapLastNameAttribute() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_LASTNAME_ATTRIBUTE);
    }

    public void setLdapLastNameAttribute(String ldapNameAttribute) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_LASTNAME_ATTRIBUTE, ldapNameAttribute);
    }

    public String getLdapProviderFullyQualifiedClassname() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_PROVIDER_FULLY_QUALIFIED_CLASSNAME);
    }

    public void setLdapProviderFullyQualifiedClassname(String ldapProviderFullyQualifiedClassname) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_PROVIDER_FULLY_QUALIFIED_CLASSNAME, ldapProviderFullyQualifiedClassname);
    }

    public String getUserFullNameFormat() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_USER_FULL_NAME_FORMAT);
    }

    public void setUserFullNameFormat(String userFullNameFormat) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_USER_FULL_NAME_FORMAT, userFullNameFormat);
    }

    public String getLdapConfigTestUsername() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_CONFIG_TEST_USERNAME);
    }

    public void setLdapConfigTestUsername(String ldapConfigTestUsername) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_CONFIG_TEST_USERNAME, ldapConfigTestUsername);
    }

    public String getLdapNarrowingFilterExpression() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_NARROWING_FILTER_EXPRESSION);
    }

    public void setLdapNarrowingFilterExpression(String ldapNarrowingFilterExpression) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_NARROWING_FILTER_EXPRESSION, ldapNarrowingFilterExpression);
    }

    public String getPersonalSpaceAllowed() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_PERSONAL_SPACE_ALLOWED);
    }

    public void setPersonalSpaceAllowed(String personalSpaceAllowed) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_PERSONAL_SPACE_ALLOWED, personalSpaceAllowed);
    }

    public BandanaManager getBandanaManager() {
        return bandanaManager;
    }

    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }
}
