/*
Copyright (c) 2006, Rajendra Kadam
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.  Redistributions
in binary form must reproduce the above copyright notice, this list of
conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.  Neither the name of
Cenqua Pty Ltd nor the names of its contributors may be used to
endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package raju.kadam.confluence.permissionmgmt.config;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.Administrative;
import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.spring.container.ContainerManager;
import org.apache.log4j.Category;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.BaseCustomPermissionConfigAction;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.CustomPermissionConstants;
import raju.kadam.util.ConfigUtil;

/**
 * Action to configure User Management Module like Jira JNDI lookup, LDAP availability etc.
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class CustomPermissionConfigAction extends BaseCustomPermissionConfigAction implements Administrative
{
    private static final Category log = Category.getInstance(CustomPermissionConfigAction.class);

    BandanaManager bandanaManager;
    BootstrapManager bootStrapManager;
    CustomPermissionConfiguration customPermissionConfiguration;
    
    
    public CustomPermissionConfigAction()
    {
    	//Get base URL for confluence installation
        bootStrapManager = (BootstrapManager) ContainerManager.getInstance().getContainerContext().getComponent("bootstrapManager");
    }
    
    public void setBandanaManager(BandanaManager bandanaManager)
    {
        this.bandanaManager = bandanaManager;
    }

    public String doDefault() throws Exception
    {
        log.debug("CustomPermissionConfigAction - Inside doDefault ..");

		configureFormValuesWithPersistedConfig();

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
        	setJiraUrl("");
        	setJiraJNDILookupKey("");
        }

        //if user is not using LDAP, then we have to set "" values to LDAPUrl and BaseDN as we can't sent null values!
        //Note null values will be passed from input as those two inputs are disabled
        if(CustomPermissionConfigConstants.DELEGATE_USER_LDAP_AUTH_KEY_NO_VALUE.equals(getLdapAuthUsed()))
        {
        	setCompanyLDAPUrl("");
        	setCompanyLDAPBaseDN("");
        }

        // Set presets as config, trimming and using defaults as needed
        setUserManagerLocation(ConfigUtil.getTrimmedStringOrNull(getUserManagerLocation()));
        setJiraUrl(ConfigUtil.getTrimmedStringOrNull(getJiraUrl()));
        setJiraJNDILookupKey(ConfigUtil.getTrimmedStringOrNull(getJiraJNDILookupKey()));
        setMaxUserIDsLimit("" + ConfigUtil.getIntOrUseDefaultIfNullOrTrimmedValueIsEmptyOrNotAnInteger("maxUserIdsLimit", getMaxUserIDsLimit(), 20));
        setUserGroupsMatchingPattern(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("userGroupsMatchingPattern", getUserGroupsMatchingPattern(), CustomPermissionConstants.SPACEKEY_REGEXP));
        setLdapAuthUsed(ConfigUtil.getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty("ldapAuthUsed", getLdapAuthUsed(), "NO"));
        setCompanyLDAPUrl(ConfigUtil.getTrimmedStringOrNull(getCompanyLDAPUrl()));
        setCompanyLDAPBaseDN(ConfigUtil.getTrimmedStringOrNull(getCompanyLDAPBaseDN()));
        setPluginDown(ConfigUtil.getTrimmedStringOrNull(getPluginDown()));
        setDownTimeMessage(ConfigUtil.getTrimmedStringOrNull(getDownTimeMessage()));
    }
    
    public boolean validateConfiguration()
	{
		log.debug("CustomPermissionConfigAction - Inside validate Configuration ...");
		
		boolean isUserManagerLocationSet = true;
		String userMgrLocation = getUserManagerLocation();
		boolean userManagerLocationIsConfluence = (userMgrLocation != null) && (userMgrLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE));
		boolean userManagerLocationIsJira = (userMgrLocation != null) && (userMgrLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE));
		
		//If userManagerLocation is not set as CONFLUENCE or JIRA, then it must be set to either value.
		if( !(userManagerLocationIsConfluence || userManagerLocationIsJira) )
		{
            addFieldError("updateUserManagerLocation", "Please indicate which application manages Wiki Users");
            isUserManagerLocationSet = false;
		}
		
		boolean isJiraUrlSet = true;
		boolean isJiraJNDISet = true; 
		//Following information needs to be check only if Wiki User Management is delegated to Jira
		if(isUserManagerLocationSet && userManagerLocationIsJira)
		{
			//check if user has set Jira URL and Jira JNDI
			if( getJiraUrl() == null || getJiraUrl().trim().equals(""))
			{
	            addFieldError("updateJiraUrl", "Enter Jira URL");
	            isJiraUrlSet = false;
			}
			
			//check if user has set Jira URL and Jira JNDI
			if( getJiraJNDILookupKey() == null || getJiraJNDILookupKey().trim().equals(""))
			{
	            addFieldError("updateJiraJNDILookupKey", "Enter Jira JNDI DataSource");
	            isJiraJNDISet = false;
			}
		}
		
		boolean isLDAPAvailable = (getLdapAuthUsed() != null) && (getLdapAuthUsed().equals(CustomPermissionConfigConstants.DELEGATE_USER_LDAP_AUTH_KEY_YES_VALUE) ? true : false);
		boolean isCompanyLDAPUrlSet = true;
		boolean isCompanyLDAPBaseDNSet = true;
		if(isLDAPAvailable)
		{
            bandanaManager.setValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY, getLdapAuthUsed().trim());

			//check if LDAP URL is set or not
			if( getCompanyLDAPUrl() == null || getCompanyLDAPUrl().trim().equals(""))
			{
	            addFieldError("updateCompanyLDAPUrl", "Enter LDAP URL");
	            isCompanyLDAPUrlSet = false;
			}

			//check if LDAP URL is set or not
			if( getCompanyLDAPBaseDN() == null || getCompanyLDAPBaseDN().trim().equals(""))
			{
	            addFieldError("updateCompanyLDAPBaseDN", "Enter LDAP Base DN");
	            isCompanyLDAPBaseDNSet = false;
			}
		}
        		
        if(isUserManagerLocationSet && isJiraUrlSet && isJiraJNDISet && isCompanyLDAPUrlSet && isCompanyLDAPBaseDNSet)
		{
			//If all above values are set that means settings are good to go!
			log.debug("CustomPermissionConfigAction - validation successful ..");
			return true;
		}
		
		log.debug("CustomPermissionConfigAction - errors during validation..");
		return false;
	}

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }

    public String getActionName(String fullClassName)
    {
        return "Configure Custom Space User Management Plugin";
    }
}