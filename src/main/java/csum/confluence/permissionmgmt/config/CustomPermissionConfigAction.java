/**
 * Copyright (c) 2007-2011, Custom Space User Management Plugin Development Team
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

import bucket.core.actions.PagerPaginationSupport;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.Administrative;
import com.opensymphony.webwork.ServletActionContext;
import csum.confluence.permissionmgmt.CustomPermissionConstants;
import csum.confluence.permissionmgmt.util.ConfigUtil;
import csum.confluence.permissionmgmt.util.cache.CacheUtil;
import csum.confluence.permissionmgmt.util.logging.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Action to configure plugin
 *
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class CustomPermissionConfigAction extends BaseCustomPermissionConfigAction {
    // tried removing deprecated Administrative interface from this and base class, but then complains about
    // missing doDefault, even though is defined and bean instantiates fine. xwork fail...

    BandanaManager bandanaManager;
    CustomPermissionConfiguration customPermissionConfiguration;
    private static final String JIRA_SOAP_PASSWORD_SET_PARAMNAME = "jiraSoapPasswordSet";


    @Autowired
    public CustomPermissionConfigAction(BandanaManager bandanaManager,
                                      CustomPermissionConfiguration customPermissionConfiguration) {
        log.debug("instantiating CustomPermissionConfigAction");
        this.bandanaManager = bandanaManager;
        this.customPermissionConfiguration = customPermissionConfiguration;

        if (bandanaManager==null) {
            // wouldn't normally log and throw but xwork is hiding errors
            log.warn("bandanaManager was not autowired in CustomPermissionConfigAction");
			throw new RuntimeException("bandanaManager was not autowired in CustomPermissionConfigAction");
        }
        else if (customPermissionConfiguration==null) {
            log.warn("customPermissionConfiguration was not autowired in CustomPermissionConfigAction");
			throw new RuntimeException("customPermissionConfiguration was not autowired in CustomPermissionConfigAction");
        }
        log.debug("instantiated CustomPermissionConfigAction");
    }

    public String doDefault() throws Exception {
        log.debug("CustomPermissionConfigAction - Inside doDefault ..");

        LegacyConfigurationMigrator.migrateLegacyConfiguration(bandanaManager);

        configureFormValuesWithPersistedConfig();

        fixFormValues();

        // NOTE: the main reason for doing this and showing errors even though it might be your first time to the page
        // is that people that have already configured the plugin once before will expect errors to show up on the page
        // when they visit it, even if they didn't post to it yet.
        if (!validateConfiguration()) {
            return ERROR;
        }

        return INPUT;
    }

    public String execute() throws Exception {
        log.debug("CustomPermissionConfigAction - Inside execute ..");

        // this is for logging logged in user info when logging error, fatal, warn
        CacheUtil.setRemoteUser(getRemoteUser());

        List resultList = new ArrayList();

        fixFormValues();

        if (!validateConfiguration()) {
            LogUtil.warnWithRemoteUserInfo(log, "Configuration was invalid");
            resultList.add(getText("csum.configure.error.configurationinvalid"));
            setActionErrors(resultList);

            return ERROR;
        }

        updatePersistedConfigWithFormValues();

        resultList.add(getText("csum.configure.success"));
        setActionMessages(resultList);

        //old way: return super.doDefault();
        return INPUT;
    }

    private void configureFormValuesWithPersistedConfig() {
        this.customPermissionConfiguration.copyTo(this);
    }

    private void updatePersistedConfigWithFormValues() {
        this.customPermissionConfiguration.updateWith(this);
    }

    public void fixFormValues() {
        // Set presets as config, trimming and using defaults if they make sense
        setDownTimeMessage(ConfigUtil.getTrimmedStringOrNull(getDownTimeMessage()));
        setGroupActionsPermitted(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("groupActionsPermitted", getGroupActionsPermitted(), CustomPermissionConfigConstants.YES));
        setMaxGroupIDsLimit("" + ConfigUtil.getIntOrUseDefaultIfNullOrTrimmedValueIsEmptyOrNotAnInteger("maxGroupIdsLimit", getMaxGroupIDsLimit(), 20));
        setNewGroupNameCreationPrefixPattern(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("newGroupNameCreationPrefixPattern", getNewGroupNameCreationPrefixPattern(), CustomPermissionConstants.DEFAULT_NEW_GROUP_NAME_PREFIX));
        setNewGroupNameCreationSuffixPattern(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("newGroupNameCreationSuffixPattern", getNewGroupNameCreationSuffixPattern(), CustomPermissionConstants.DEFAULT_NEW_GROUP_NAME_SUFFIX));
        setMaxUserIDsLimit("" + ConfigUtil.getIntOrUseDefaultIfNullOrTrimmedValueIsEmptyOrNotAnInteger("maxUserIdsLimit", getMaxUserIDsLimit(), 20));
        setPersonalSpaceAllowed(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("personalSpaceAllowed", getPersonalSpaceAllowed(), CustomPermissionConfigConstants.NO));
        setPluginDown(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("pluginDown", getPluginDown(), CustomPermissionConfigConstants.NO));
        setUserSearchEnabled(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("userSearchEnabled", getUserSearchEnabled(), CustomPermissionConfigConstants.YES));
        setGroupMembershipRefreshFixEnabled(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("groupMembershipRefreshFixEnabled", getGroupMembershipRefreshFixEnabled(), CustomPermissionConfigConstants.NO));
        setNumRowsPerPage("" + ConfigUtil.getIntOrUseDefaultIfNullOrTrimmedValueIsEmptyOrNotAnIntegerOrUseRangeMinOrMaxIfOutOfRange("numRowsPerPage", getNumRowsPerPage(), PagerPaginationSupport.DEFAULT_COUNT_ON_EACH_PAGE, CustomPermissionConfigConstants.MIN_ROWS_PER_PAGE, CustomPermissionConfigConstants.MAX_ROWS_PER_PAGE));
        setUnvalidatedUserAdditionEnabled(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("unvalidatedUserAdditionEnabled", getUnvalidatedUserAdditionEnabled(), CustomPermissionConfigConstants.NO));
        
        // only relevant for page itself, so not putting into context
        Map paramMap = ServletActionContext.getRequest().getParameterMap();
        log.debug("paramMap: " + paramMap);
        if (paramMap.get(JIRA_SOAP_PASSWORD_SET_PARAMNAME) == null) {
            // make sure password is set to null if jiraSoapPasswordSet is not checked/set
            setJiraSoapPassword(null);
        }
    }

    public boolean validateConfiguration() {
        log.debug("CustomPermissionConfigAction - Inside validate Configuration ...");
        CsumConfigValidationResponse validResp = CustomPermissionConfiguration.validate(this, this.customPermissionConfiguration, getRemoteUser().getName(), this, true);
        Map fieldErrorMap = validResp.getFieldNameToErrorMessage();
        Iterator keys = fieldErrorMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            addFieldError(key, (String) fieldErrorMap.get(key));
        }

        return validResp.isValid();
    }

    public String getActionName(String fullClassName) {
        return "Configure Custom Space User Management Plugin";
    }


}