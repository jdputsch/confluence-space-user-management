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

package raju.kadam.confluence.permissionmgmt;

import bucket.container.ContainerManager;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.Administrative;
import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Category;

/**
 * Action to configure User Management Module like Jira JNDI lookup, LDAP availability etc.
 */
public class CustomPermissionConfigAction extends ConfluenceActionSupport implements Administrative
{
    private static final Category log = Category.getInstance(CustomPermissionConfigAction.class);

    BandanaManager bandanaManager;
    BootstrapManager bootStrapManager;

    String v_userManagerLocation;
    String updateUserManagerLocation;
    
    String v_jiraUrl;
    String updateJiraUrl;
    
    String v_jiraJNDILookupKey;
    String updateJiraJNDILookupKey;
    
    String v_isLdapAuthUsed;
    String updateIsLdapAuthUsed;
    
    String v_companyLDAPUrl;
    String updateCompanyLDAPUrl;
    
    String v_companyLDAPBaseDN;
    String updateCompanyLDAPBaseDN;

    String v_maxUserIDsLimit; 
    String updateMaxUserIDsLimit;

    String v_userGroupsMatchingPattern;
    String updateUserGroupsMatchingPattern;
    
    String v_isPluginDown;
    String updateIsPluginDown;
    
    String v_downTimeMessage;
    String updateDownTimeMessage;
    
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

		//Get initial values from Bandana Manager
        v_userManagerLocation = getUserManagerLocation();
        v_jiraUrl = getJiraUrl();
        v_jiraJNDILookupKey = getJiraJNDILookupKey();

        v_maxUserIDsLimit = getMaxUserIDsLimit();
        //If maxUserIDs limit is not set, then assign default value 
        if( (v_maxUserIDsLimit == null) || (v_maxUserIDsLimit.trim().equals("")))
        {
        	bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT, "20");
        }
        
        v_userGroupsMatchingPattern = getUserGroupsMatchingPattern();
        //If group matching pattern is not set, then assign default pattern as "SPACEKEY-.*" which will match only usergroups specific for that wiki space 
        if( (v_userGroupsMatchingPattern == null) || (v_userGroupsMatchingPattern.trim().equals("")))
        {
            bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_USERGROUPS_MATCHING_PATTERN, "SPACEKEY-.*");
        }
        
        v_isLdapAuthUsed = getIsLdapAuthUsed();
        //"LDAP Authentication Used?: " has Default Value "NO" 
        if( (v_isLdapAuthUsed == null) || (v_isLdapAuthUsed.trim().equals("")))
        {
        	bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY, "NO");
        }

        v_companyLDAPUrl = (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY);
        v_companyLDAPBaseDN = (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY);
        v_isPluginDown = (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_PLUGIN_STATUS);
        v_downTimeMessage = (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_DOWNTIME_MESSAGE);

        //Also set values for update Variables so that they will display values correctly!.
        setUpdateUserManagerLocation(v_userManagerLocation);
        setUpdateJiraUrl(v_jiraUrl);
        setUpdateJiraJNDILookupKey(v_jiraJNDILookupKey);
        setUpdateMaxUserIDsLimit(v_maxUserIDsLimit);  
        setUpdateUserGroupsMatchingPattern(v_userGroupsMatchingPattern);
        setUpdateIsLdapAuthUsed(v_isLdapAuthUsed);
        setUpdateCompanyLDAPUrl(v_companyLDAPUrl);
        setUpdateCompanyLDAPBaseDN(v_companyLDAPBaseDN);
        setUpdateDownTimeMessage(v_downTimeMessage);
        setUpdateIsPluginDown(v_isPluginDown);
        
        return super.doDefault();
    }
	
    public String execute() throws Exception
    {
		log.debug("CustomPermissionConfigAction - Inside execute ..");
    	
    	if(!validateConfiguration()) return ERROR;
    	setPluginValues();
    	
        return SUCCESS;
    }

    public void setPluginValues()
    {
		log.debug("CustomPermissionConfigAction - Set Plugin Values..");
		
        if (TextUtils.stringSet(updateUserManagerLocation))
        {
        	bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_USER_MANAGER_LOCATION, updateUserManagerLocation.trim());
        }

        //if CONFLUENCE used for user management, then we have to set "" values to jiraUrl and jiraJNDILookupKey as we can't sent null values!
        //Note null values will be passed from input as those two inputs are disabled
        if(updateUserManagerLocation.equals(DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE))
        {
        	updateJiraUrl = "";
        	updateJiraJNDILookupKey = "";
        }
        
        //Since this value can contain null string, we are setting as whatever input value we get
        bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_JIRA_URL, updateJiraUrl.trim());
        bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_JIRA_JNDI_KEY, updateJiraJNDILookupKey.trim());

        if (TextUtils.stringSet(updateIsLdapAuthUsed))
        {
            bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY, updateIsLdapAuthUsed.trim());
        }

        v_isLdapAuthUsed = (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY);
        //if user is not using LDAP, then we have to set "" values to LDAPUrl and BaseDN as we can't sent null values!
        //Note null values will be passed from input as those two inputs are disabled
        if(v_isLdapAuthUsed.equals(DELEGATE_USER_LDAP_AUTH_KEY_NO_VALUE))
        {
        	updateCompanyLDAPUrl = "";
        	updateCompanyLDAPBaseDN = "";
        }
        
        bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY, updateCompanyLDAPUrl.trim());
        bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY, updateCompanyLDAPBaseDN.trim());

        if (TextUtils.stringSet(updateMaxUserIDsLimit))
        {
            bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT, updateMaxUserIDsLimit.trim());
        }

        if (TextUtils.stringSet(updateUserGroupsMatchingPattern))
        {
            bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_USERGROUPS_MATCHING_PATTERN, updateUserGroupsMatchingPattern.trim());
        }

        if (TextUtils.stringSet(updateIsPluginDown))
        {
            bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_PLUGIN_STATUS, updateIsPluginDown.trim());
        }

        if (TextUtils.stringSet(updateDownTimeMessage))
        {
            bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_DOWNTIME_MESSAGE, updateDownTimeMessage.trim());
        }
    }
    
	public boolean validateConfiguration()
	{
		log.debug("CustomPermissionConfigAction - Inside validate Configuration ...");
		
		boolean isUserManagerLocationSet = true;
		String userMgrLocation = getUpdateUserManagerLocation();
		boolean userManagerLocationIsConfluence = (userMgrLocation != null) && (userMgrLocation.equals(DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE));
		boolean userManagerLocationIsJira = (userMgrLocation != null) && (userMgrLocation.equals(DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE));
		
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
			if( getUpdateJiraUrl() == null || getUpdateJiraUrl().trim().equals(""))
			{
	            addFieldError("updateJiraUrl", "Enter Jira URL");
	            isJiraUrlSet = false;
			}
			
			//check if user has set Jira URL and Jira JNDI
			if( getUpdateJiraJNDILookupKey() == null || getUpdateJiraJNDILookupKey().trim().equals(""))
			{
	            addFieldError("updateJiraJNDILookupKey", "Enter Jira JNDI DataSource");
	            isJiraJNDISet = false;
			}
		}
		
		boolean isLDAPAvailable = (getUpdateIsLdapAuthUsed() != null) && (getUpdateIsLdapAuthUsed().equals(CustomPermissionConfigAction.DELEGATE_USER_LDAP_AUTH_KEY_YES_VALUE) ? true : false);
		boolean isCompanyLDAPUrlSet = true;
		boolean isCompanyLDAPBaseDNSet = true;
		if(isLDAPAvailable)
		{
            bandanaManager.setValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY, getUpdateIsLdapAuthUsed().trim());

			//check if LDAP URL is set or not
			if( getUpdateCompanyLDAPUrl() == null || getUpdateCompanyLDAPUrl().trim().equals(""))
			{
	            addFieldError("updateCompanyLDAPUrl", "Enter LDAP URL");
	            isCompanyLDAPUrlSet = false;
			}

			//check if LDAP URL is set or not
			if( getUpdateCompanyLDAPBaseDN() == null || getUpdateCompanyLDAPBaseDN().trim().equals(""))
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
    
	public String getUserManagerLocation() {
		return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_USER_MANAGER_LOCATION);
	}

    public String getUpdateUserManagerLocation() {
		return updateUserManagerLocation;
	}
    
    public void setUpdateUserManagerLocation(String updateUserManagerLocation) {
		this.updateUserManagerLocation = updateUserManagerLocation;
	}

    public String getJiraUrl() {
    	return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_JIRA_URL);
	}
	
	public String getUpdateJiraUrl() {
		return updateJiraUrl;
	}

	public void setUpdateJiraUrl(String updateJiraUrl) {
		this.updateJiraUrl = updateJiraUrl;
	}

    public String getJiraJNDILookupKey()
    {
    	return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_JIRA_JNDI_KEY);
    }

    public String getUpdateJiraJNDILookupKey()
    {
        return updateJiraJNDILookupKey;
    }
    
    public void setUpdateJiraJNDILookupKey(String updateJiraJNDILookupKey)
    {
        this.updateJiraJNDILookupKey = updateJiraJNDILookupKey;
    }
    
	public String getMaxUserIDsLimit() {
		return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT);
	}
	
	public String getUpdateMaxUserIDsLimit() {
		return updateMaxUserIDsLimit;
	}

	public void setUpdateMaxUserIDsLimit(String updateMaxUserIDsLimit) {
		this.updateMaxUserIDsLimit = updateMaxUserIDsLimit;
	}

	public String getUserGroupsMatchingPattern() {
		return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_USERGROUPS_MATCHING_PATTERN);
	}
	
	public String getUpdateUserGroupsMatchingPattern() {
		return updateUserGroupsMatchingPattern;
	}
	
	public void setUpdateUserGroupsMatchingPattern(String userGroupsMatchingPattern) {
		this.updateUserGroupsMatchingPattern = userGroupsMatchingPattern;
	}
	
	public String getIsLdapAuthUsed()
    {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY);
    }

	public String getUpdateIsLdapAuthUsed() {
		return updateIsLdapAuthUsed;
	}

	public void setUpdateIsLdapAuthUsed(String updateCompanyLDAPUrl) {
		this.updateIsLdapAuthUsed = updateCompanyLDAPUrl;
	}
	
	public String getCompanyLDAPUrl() {
		return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY);
	}
	
	public String getUpdateCompanyLDAPUrl() {
		return updateCompanyLDAPUrl;
	}

	public void setUpdateCompanyLDAPUrl(String updateCompanyLDAPUrl) {
		this.updateCompanyLDAPUrl = updateCompanyLDAPUrl;
	}

	public String getCompanyLDAPBaseDN() {
		return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY);
	}
	
	public String getUpdateCompanyLDAPBaseDN() {
		return updateCompanyLDAPBaseDN;
	}

	public void setUpdateCompanyLDAPBaseDN(String updateCompanyLDAPBaseDN) {
		this.updateCompanyLDAPBaseDN = updateCompanyLDAPBaseDN;
	}

	public String getIsPluginDown() {
		return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_PLUGIN_STATUS);
	}

	public String getUpdateIsPluginDown() {
		return updateIsPluginDown;
	}

	public void setUpdateIsPluginDown(String updateIsPluginDown) {
		this.updateIsPluginDown = updateIsPluginDown;
	}

	public String getDownTimeMessage() {
		return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), DELEGATE_USER_MGMT_DOWNTIME_MESSAGE);
	}

	public String getUpdateDownTimeMessage() {
		return updateDownTimeMessage;
	}

	public void setUpdateDownTimeMessage(String updateDownTimeMessage) {
		this.updateDownTimeMessage = updateDownTimeMessage;
	}
	
    public String getActionName(String fullClassName)
    {
        return "Configure Custom Space User Management Plugin";
    }


}