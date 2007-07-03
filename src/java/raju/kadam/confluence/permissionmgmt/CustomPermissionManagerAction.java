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

import java.util.*;

import raju.kadam.util.*;
import raju.kadam.confluence.permissionmgmt.service.*;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedQueryType;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;
import raju.kadam.confluence.permissionmgmt.util.GroupNameUtil;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAdministrative;
import com.atlassian.confluence.spaces.persistence.dao.SpaceDao;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.confluence.util.SpaceComparator;
import com.atlassian.user.User;
import com.opensymphony.webwork.ServletActionContext;

/**
 * 
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class CustomPermissionManagerAction extends AbstractSpaceAction implements SpaceAdministrative
{
    private BandanaManager bandanaManager;
    private SpaceDao spaceDao;
    private CustomPermissionServiceManager customPermissionServiceManager;
    private CustomPermissionConfiguration customPermissionConfiguration;
    private String selectedGroup;
    private String userSearch;
    private boolean userSearchFormFilled;
    private AdvancedUserQuery advancedUserQuery;

    public CustomPermissionManagerAction()
	{
		log.debug("CustomPermissionManagerAction start constructor");
        log.debug("CustomPermissionManagerAction end constructor");
    }
    
    public void setBandanaManager(BandanaManager bandanaManager)
    {
        this.bandanaManager = bandanaManager;
    }
    
	public String doDefault() throws Exception
    {
		//This method will be called very first time when user access .../custompermissionsmanage.action?key=<SPACEKEY>
		//It will display screen with all user groups as per usergroupsPattern setting in Configuration
		log.debug("CustomPermissionManagerAction - log - Inside doDefault ..");
        return super.doDefault();
    }

    private String getParameterValue(Map paramMap, String param) {
        String[] values = (String[])paramMap.get(param);
        if ( values != null && values.length > 0 ) {
            return values[0];
        }
        return null;
    }

    private List getParameterValues(Map paramMap, String param) {
        String[] values = (String[])paramMap.get(param);
        if ( values != null && values.length > 0 ) {
            return Arrays.asList(values);
        }

        return new ArrayList();
    }

    public ServiceContext createServiceContext() {
        ServiceContext context = new ServiceContext();
        context.setLoggedInUser(getRemoteUser().getName());
        context.setSpace(this.getSpace());
        return context;
    }

    public CustomPermissionManagerActionContext createContext() {
        CustomPermissionManagerActionContext context = new CustomPermissionManagerActionContext();
        Map paramMap = ServletActionContext.getRequest().getParameterMap();
        // TODO: consider converting selectedGroup from List to single element
        context.setSelectedGroup(getParameterValue( paramMap, "selectedGroup"));
        context.setGroupToAdd(getParameterValue( paramMap, "groupToAdd"));
        context.setGroupToRemove(getParameterValue( paramMap, "groupToRemove"));
        context.setUsersToAdd(StringUtil.convertColonSemicolonOrCommaDelimitedStringToList(getParameterValue( paramMap, "usersToAdd")));
        context.setUsersToRemove(StringUtil.convertColonSemicolonOrCommaDelimitedStringToList(getParameterValue( paramMap, "usersToRemove")));
        context.setLoggedInUser(getRemoteUser().getName());
		context.setKey(getKey());
    	context.setAdminAction(getParameterValue( paramMap, "adminAction"));
        context.setUserSearch(getParameterValue( paramMap, "userSearch"));
        return context;
    }

    public AdvancedUserQuery createAdvancedUserQuery() {
        AdvancedUserQuery userQuery = new AdvancedUserQuery();
        Map paramMap = ServletActionContext.getRequest().getParameterMap();
        userQuery.setEmailSearchType(getParameterValue( paramMap, "emailSearchType"));
        userQuery.setFullNameSearchType(getParameterValue( paramMap, "fullNameSearchType"));
        userQuery.setGroupNameSearchType(getParameterValue( paramMap, "groupNameSearchType"));
        userQuery.setUserNameSearchType(getParameterValue( paramMap, "userNameSearchType"));
        userQuery.setPartialEmail(getParameterValue( paramMap, "partialEmail"));
        userQuery.setPartialFullName(getParameterValue( paramMap, "partialFullName"));
        userQuery.setPartialGroupName(getParameterValue( paramMap, "partialGroupName"));
        userQuery.setPartialUserName(getParameterValue( paramMap, "partialUserName"));
        return userQuery;
    }

    public String execute() throws Exception
    {
		log.debug("CustomPermissionManagerAction - log - Inside execute...");

        CustomPermissionManagerActionContext context = createContext();

        setSelectedGroup(context.getSelectedGroup());
        setUserSearch(context.getUserSearch());

        AdvancedUserQuery advancedUserQuery = createAdvancedUserQuery();
        setAdvancedUserQuery(advancedUserQuery);

        setUserSearchFormFilled(advancedUserQuery.isValid());

        // TODO: rewrite validation and include errors in display.vm
        //Validate user input
        //boolean isValid = validateInput(context);
    	//if(!isValid)
        //{
        //    log.debug("Input was invalid");
        //    return ERROR;
        //}

        // TODO: rewrite validation and include errors in display.vm
        //String userGroupsValidationMessage = validateUserGroupWikiSpaceAssociation(context.getSelectedGroups());
    	//if(userGroupsValidationMessage != null)
    	//{
        //    log.debug("There are no groups this user can currently administer. message=" + userGroupsValidationMessage);
        //    addFieldError("NotPermittedUserGroupsErrorMessage", userGroupsValidationMessage);
    	//	return ERROR;
    	//}

        return manage(context);
    }

    
    
    //If input group name matches with user select group, then return true
    //This function will be useful to remember user selection of checkbox during displaying errors!
    //public boolean isGroupSelected(String groupName)
    //{
    //	if( selectedUserGroupsList!=null && selectedUserGroupsList.contains(groupName))
    //	{
    //		return true;
    //	}
    //
    //  return false;
    //}

    /*
     * Action implements SpaceAdministrative interface.
     * To make sure that only Space Administrators, System checks information from getPermissionTypes() and isPermitted() functions 
     * Hence we have implemented those two functions below!
     */
    public List getPermissionTypes()
    {
        List permissionTypes = super.getPermissionTypes();

        if (getSpace() != null)
        {
            if (this instanceof SpaceAdministrative)
            {
                addPermissionTypeTo(SpacePermission.ADMINISTER_SPACE_PERMISSION, permissionTypes);
            }
        }
        return permissionTypes;
    }

    public boolean isPermitted()
    {
        if (GeneralUtil.isSuperUser(getRemoteUser())) {
            return true;
        }

        return spacePermissionManager.hasPermission(getPermissionTypes(), getSpace(), getRemoteUser());
    }

    /*
     * Get List of all Spaces to which logged in user is Space Administrator
     */
    public List getSpacesAsSpaceAdminForUser()
    {
    	return getSpacesAssociatedToUserForGivenPermission(getRemoteUser(),SpacePermission.ADMINISTER_SPACE_PERMISSION);
    }
    
    public List getSpacesAssociatedToUserForGivenPermission(User user, String permission)
    {
        if (GeneralUtil.isSuperUser(user))
            return getAllSpaces();
   
        List permittedSpacesForUser = spaceDao.getPermittedSpacesForUser(user,permission);
        Collections.sort(permittedSpacesForUser, new SpaceComparator());

        return permittedSpacesForUser;
    }
    
    /*
     * If logged in user is Confluence Administrator, then get list of wiki spaces available in Confluence
     */
    public List getAllSpaces()
    {
        return spaceDao.findAllSorted("name");
    }

    //reduce unnecessary number of variables going to services
    public ServiceContext createServiceContext(CustomPermissionManagerActionContext context) {
        ServiceContext result = new ServiceContext();
        result.setLoggedInUser(context.getLoggedInUser());
        result.setSpace(getSpace());
        return result;
    }

    public String manage(CustomPermissionManagerActionContext context)
    {
		List resultList = new ArrayList();
        String opMessage = null;
		String adminAction = context.getAdminAction();

        try
        {
            GroupManagementService groupManagementService = getGroupManagementService();
            UserManagementService userManagementService = getUserManagementService();
            ServiceContext serviceContext = createServiceContext();

            if(adminAction != null)
            {

                if(adminAction.equals("addUsersToGroup"))
                {
                    userManagementService.addUsersByUsernameToGroup(context.getUsersToAdd(), context.getSelectedGroup(), serviceContext);

                    opMessage = "<font color=\"green\">User(s) " + StringUtil.convertCollectionToCommaDelimitedString(context.getUsersToAdd()) + " added to group " + context.getSelectedGroup() + " successfully!</font>";
                }
                else if(adminAction.equals("removeUsersFromGroup"))
                {
                    userManagementService.removeUsersByUsernameFromGroup(context.getUsersToRemove(), context.getSelectedGroup(), serviceContext);

                    opMessage = "<font color=\"green\">User(s) " + StringUtil.convertCollectionToCommaDelimitedString(context.getUsersToRemove()) + " removed from group " + context.getSelectedGroup() + " successfully!</font>";
                }
                else if(adminAction.equals("addGroup"))
                {
                    groupManagementService.addGroup(context.getGroupToAdd(), serviceContext);

                    opMessage = "<font color=\"green\">Group " + context.getGroupToAdd() + " added successfully!</font>";
                }
                else if(adminAction.equals("removeGroup"))
                {
                    groupManagementService.removeGroup(context.getGroupToRemove(), serviceContext);

                    opMessage = "<font color=\"green\">Group " + context.getGroupToRemove() + " removed successfully!</font>";
                }                
            }

            // note: is normal at times not to have an action (selecting group for example)
        }
        catch(ServiceException e)
        {
            e.printStackTrace();
            resultList.add("" + e.getMessage());
            setActionErrors(resultList);
            return ERROR;
        }

        if (opMessage!=null) {
            resultList.add(opMessage);
            setActionMessages(resultList);
            return SUCCESS;
        }

        return INPUT;
    }

    public String getUserManagerLocation() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_USER_MANAGER_LOCATION);
	}
    
	public String getJiraUrl() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_URL);
	}
   
	public String getIsLdapAuthUsed()
    {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY);
    }
	
    public String getJiraJNDILookupKey()
    {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_JNDI_KEY);
    }

	public String getCompanyLDAPBaseDN() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY);
	}

	public String getCompanyLDAPUrl() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY);
	}
	
	//Get the count which indicates total no. of userids that can be processed at a time.
	public String getMaxUserIDsLimit() {
        return ((String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT));
	}

	public String getIsPluginDown() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_PLUGIN_STATUS);
	}
	
	//Validate user input. return false if data invalid.
    /*
    public boolean validateInput(CustomPermissionManagerActionContext context)
    {
        log.debug("Validating form. " + context);

        boolean isValid = true; //By default validation will be successful

        //Will use default of 20 if we don't validate max UserIDLimit during configuration or user has changed value by modifying xml file (confluence-home/config/confluence-global.bandana.xml).
        int maxUserIDLimit = ConfigUtil.getIntOrUseDefaultIfNullOrTrimmedValueIsEmptyOrNotAnInteger("maxUserIDLimit", getMaxUserIDsLimit(), 20);

        String adminAction = context.getAdminAction();

        if(adminAction == null)
    	{
    		//user has not selected which action to perform.
            addFieldError("adminAction", "Please select an action.");
            isValid = false;
        }
        else if(adminAction.equals("AddToGroups")) {

            if(!isGroupSelected(context.getParamMap()))
            {
                addFieldError("groups_", "Please select at least one group to which you want to add users.");
                isValid = false;
            }
            else if(context.getUsersToAddList() == null || context.getUsersToAddList().size()==0)
            {
                addFieldError("users", "Please enter usernames you'd like to add to the group.");
                isValid = false;
            }
            else
            {
                if( ListUtil.isListSizeOverMaxNum( context.getUsersToAddList(), maxUserIDLimit ) )
                {
                    addFieldError("users", "Only "+ maxUserIDLimit + " users will be processed at a time.");
                    isValid = false;
                }
            }
        }
        else if(adminAction.equals("RemoveFromGroups")) {

            if(!isGroupSelected(context.getParamMap()))
            {
                addFieldError("groups_", "Please select at least one group from which you want to remove users.");
                isValid = false;
            }
            else if(context.getUsersToAddList() == null || context.getUsersToAddList().size()==0)
            {
                addFieldError("users", "Please select users you wish to remove from the group.");
                isValid = false;
            }
            else
            {
                if( ListUtil.isListSizeOverMaxNum( context.getUsersToAddList(), maxUserIDLimit ) )
                {
                    addFieldError("users", "Only "+ maxUserIDLimit + " users will be processed at a time.");
                    isValid = false;
                }
            }
        }
        else if(adminAction.equals("AddGroups"))
        {
            if(context.getGroupsToAddList() == null || context.getGroupsToAddList().size()==0)
            {
                addFieldError("groupsToAdd", "Please enter one or more groups to add, separated by commas.");
                isValid = false;
            }
            else
            {
                if( ListUtil.isListSizeOverMaxNum( context.getGroupsToAddList(), maxUserIDLimit ) )
                {
                    addFieldError("groupsToAdd", "Only "+ maxUserIDLimit + " groups will be processed at a time.");
                    isValid = false;
                }
            }
        }
        else if(adminAction.equals("RemoveGroups"))
        {
            if(!isGroupSelected(context.getParamMap()))
            {
                addFieldError("groups_", "Please select at least one group to remove.");
                isValid = false;
            }
        }
        else {
    		addFieldError("adminAction", "'" + adminAction + "' is not a valid action!");
            isValid = false;
        }

        return isValid;
    }
    */

    //Get total user count for given user group
    //public int findUserCountForUserGroup(String grpName)
    //{
    //	return PagerUtils.count(userAccessor.getMemberNames(userAccessor.getGroup(grpName)));
    //}

    public boolean getIsPluginConfigurationDone() {
        boolean isConfigValid = this.getCustomPermissionConfiguration().isValid();
        log.debug("isPluginConfigurationDone = " + isConfigValid);
        return isConfigValid;
    }

    public boolean getIsGroupActionsPermitted() {
        boolean isGroupActionsPermitted = false;
        String groupActionsPermitted = getCustomPermissionConfiguration().getGroupActionsPermitted();
        if (ConfigUtil.isNotNullAndIsYesOrNo(groupActionsPermitted)) {
            if ("YES".equals(groupActionsPermitted)) {
                isGroupActionsPermitted = true;
            }
        }
        log.debug("isGroupActionsPermitted = " + isGroupActionsPermitted);
        return isGroupActionsPermitted;
    }

    public GroupManagementService getGroupManagementService() throws ServiceException {
        return getCustomPermissionServiceManager().getGroupManagementService();
    }

    public UserManagementService getUserManagementService() throws ServiceException {
        return getCustomPermissionServiceManager().getUserManagementService();
    }

    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(String selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public CustomPermissionServiceManager getCustomPermissionServiceManager() {
        return customPermissionServiceManager;
    }

    public void setCustomPermissionServiceManager(CustomPermissionServiceManager customPermissionServiceManager) {
        this.customPermissionServiceManager = customPermissionServiceManager;
    }    

    public SpaceDao getSpaceDao() {
        return spaceDao;
    }

    public void setSpaceDao(SpaceDao spaceDao) {
        this.spaceDao = spaceDao;
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }

    public List getGroups() {
        try {
            ServiceContext serviceContext = createServiceContext();
            return this.getGroupManagementService().findGroups(serviceContext);
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return new ArrayList();
    }

    public List findUsers(String groupName) {
        try {
            ServiceContext serviceContext = createServiceContext();
            return this.getUserManagementService().findUsersForGroup(groupName, serviceContext);
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return new ArrayList();
    }

    public String getNewGroupPrefix() {
        return GroupNameUtil.replaceSpaceKey(getCustomPermissionConfiguration().getNewGroupNameCreationPrefixPattern(), space.getKey());
    }

    public String getNewGroupSuffix() {
        return GroupNameUtil.replaceSpaceKey(getCustomPermissionConfiguration().getNewGroupNameCreationSuffixPattern(), space.getKey());
    }

    public List findUsersWhoseNameStartsWith(String partialName) {
        try {
            ServiceContext serviceContext = createServiceContext();
            return this.getUserManagementService().findUsersWhoseNameStartsWith(partialName, serviceContext);
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return new ArrayList();
    }

    //TODO: is there a better way to access this?
    public String getSubstringContains() {
        return AdvancedQueryType.SUBSTRING_CONTAINS;
    }

    //TODO: is there a better way to access this?
    public String getSubstringEndsWith() {
        return AdvancedQueryType.SUBSTRING_ENDS_WITH;
    }

    //TODO: is there a better way to access this?
    public String getSubstringStartsWith() {
        return AdvancedQueryType.SUBSTRING_STARTS_WITH;
    }

    //TODO: is there a better way to access this?
    public String getUserSearch() {
        return userSearch;
    }

    //TODO: is there a better way to access this?
    public void setUserSearch(String userSearch) {
        this.userSearch = userSearch;
    }

    public boolean getUserSearchFormFilled() {
        return userSearchFormFilled;
    }

    public void setUserSearchFormFilled(boolean userSearchFormFilled) {
        this.userSearchFormFilled = userSearchFormFilled;
    }

    public AdvancedUserQuery getAdvancedUserQuery() {
        return advancedUserQuery;
    }

    public void setAdvancedUserQuery(AdvancedUserQuery advancedUserQuery) {
        this.advancedUserQuery = advancedUserQuery;
    }

    private void addFieldErrorIfMessageNotNull(String nameOfField, String error) {
        if (error!=null) {
            log.warn("setting fieldError " + error + " on " + nameOfField);
            addFieldError(nameOfField, error);
        }
    }

    public List findUsersAdvanced() {
        try {
            ServiceContext serviceContext = createServiceContext();
            AdvancedUserQuery query = getAdvancedUserQuery();
            query.makeSearchTypesMatchTermQueryConstantInstances();            
            AdvancedUserQueryResults results = this.getUserManagementService().findUsers(query, serviceContext);

            addFieldErrorIfMessageNotNull("partialEmail",results.getEmailFieldMessage());
            addFieldErrorIfMessageNotNull("partialFullName",results.getFullNameFieldMessage());
            addFieldErrorIfMessageNotNull("partialGroupName",results.getGroupNameFieldMessage());
            addFieldErrorIfMessageNotNull("partialUserName",results.getUserNameFieldMessage());

            return results.getUsers();
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return new ArrayList();
    }

    public boolean isMemberOfSelectedGroup(String userName) {
        boolean result = false;

        try {
            result = this.getUserManagementService().isMemberOf(userName, this.getSelectedGroup());
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    public String getActionName(String fullClassName)
    {
    	return "Custom Space Usergroups Manager";
    }
  }