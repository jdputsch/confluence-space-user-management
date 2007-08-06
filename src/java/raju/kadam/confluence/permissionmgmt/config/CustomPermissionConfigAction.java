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

package raju.kadam.confluence.permissionmgmt.config;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.Administrative;
import com.atlassian.confluence.setup.BootstrapManager;
import org.apache.log4j.Category;
import raju.kadam.confluence.permissionmgmt.CustomPermissionConstants;
import raju.kadam.confluence.permissionmgmt.util.PropsUtil;
import raju.kadam.confluence.permissionmgmt.util.ConfigUtil;

import java.util.Map;
import java.util.Iterator;

/**
 * Action to configure plugin
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class CustomPermissionConfigAction extends BaseCustomPermissionConfigAction implements Administrative
{
    private static final Category log = Category.getInstance(CustomPermissionConfigAction.class);

    BandanaManager bandanaManager;
    BootstrapManager bootstrapManager;
    CustomPermissionConfiguration customPermissionConfiguration;
    
    
    public CustomPermissionConfigAction()
    {
    	log.debug("CustomPermissionConfigAction instance created");
    }
    
    public void setBandanaManager(BandanaManager bandanaManager)
    {
        this.bandanaManager = bandanaManager;
    }

    public String doDefault() throws Exception
    {
        log.debug("CustomPermissionConfigAction - Inside doDefault ..");

		configureFormValuesWithPersistedConfig();

        // NOTE: the main reason for doing this and showing errors even though it might be your first time to the page
        // is that people that have already configured the plugin once before will expect errors to show up on the page
        // when they visit it, even if they didn't post to it yet.
        if(!validateConfiguration()) {
            return ERROR;
        }

        fixFormValues();

        return super.doDefault();
    }

    public String execute() throws Exception
    {
		log.debug("CustomPermissionConfigAction - Inside execute ..");
    	
    	if(!validateConfiguration()) {
            return ERROR;
        }

        fixFormValues();

        updatePersistedConfigWithFormValues();
    	
        return SUCCESS;
    }

    private void configureFormValuesWithPersistedConfig()
    {
        getCustomPermissionConfiguration().copyTo(this);
    }

    private void updatePersistedConfigWithFormValues()
    {        
        getCustomPermissionConfiguration().updateWith(this);
    }

    public void fixFormValues()
    {
        //if CONFLUENCE used for user management, then we have to set "" values to jiraUrl and jiraJNDILookupKey as we can't sent null values!
        //Note null values will be passed from input as those two inputs are disabled
        if(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE.equals(getUserManagerLocation()))
        {
        	setJiraJNDILookupKey("");
        }

        // Set presets as config, trimming and using defaults as needed
        setUserManagerLocation(ConfigUtil.getTrimmedStringOrNull(getUserManagerLocation()));
        setJiraJNDILookupKey(ConfigUtil.getTrimmedStringOrNull(getJiraJNDILookupKey()));
        setMaxUserIDsLimit("" + ConfigUtil.getIntOrUseDefaultIfNullOrTrimmedValueIsEmptyOrNotAnInteger("maxUserIdsLimit", getMaxUserIDsLimit(), 20));
        setUserGroupsMatchingPattern(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("userGroupsMatchingPattern", getUserGroupsMatchingPattern(), CustomPermissionConstants.SPACEKEY_REGEXP));
        setLdapAuthUsed(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("ldapAuthUsed", getLdapAuthUsed(), "NO"));
        setPluginDown(ConfigUtil.getTrimmedStringOrNull(getPluginDown()));
        setDownTimeMessage(ConfigUtil.getTrimmedStringOrNull(getDownTimeMessage()));
    }
    
    public boolean validateConfiguration()
	{
		log.debug("CustomPermissionConfigAction - Inside validate Configuration ...");
        ConfigValidationResponse validResp = CustomPermissionConfiguration.validate(this);
        Map fieldErrorMap = validResp.getFieldNameToErrorMessage();
        Iterator keys = fieldErrorMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String)keys.next();
            addFieldError(key, (String)fieldErrorMap.get(key));
        }

        return validResp.isValid();
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }

    public String getJiraPropertiesFilename() {
        return PropsUtil.PROPS_FILENAME;
    }

    public String getPropertyNameForJiraSoapPassword() {
        return CustomPermissionConfigConstants.PROPERTIES_FILE_PROPERTY_NAME_JIRA_SOAP_PASSWORD;
    }

    public String getPropertyNameForJiraSoapUrl() {
        return CustomPermissionConfigConstants.PROPERTIES_FILE_PROPERTY_NAME_JIRA_SOAP_URL;
    }

    public String getPropertyNameForJiraSoapUsername() {
        return CustomPermissionConfigConstants.PROPERTIES_FILE_PROPERTY_NAME_JIRA_SOAP_USERNAME;
    }        

    public String getActionName(String fullClassName)
    {
        return "Configure Custom Space User Management Plugin";
    }
}