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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.sql.DataSource;

import org.apache.xmlrpc.XmlRpcClient;

import raju.kadam.util.*;
import raju.kadam.util.LDAP.LDAPUser;
import raju.kadam.util.LDAP.LDAPUtil;
import raju.kadam.confluence.permissionmgmt.service.GroupManagementService;
import raju.kadam.confluence.permissionmgmt.util.GroupMatchingUtil;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAdministrative;
import com.atlassian.confluence.spaces.persistence.dao.SpaceDao;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.confluence.util.SpaceComparator;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.webwork.ServletActionContext;

/**
 * 
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class CustomPermissionManagerAction extends AbstractSpaceAction implements SpaceAdministrative
{
    public static final String RPC_PATH  = "/rpc/xmlrpc";

    private BootstrapManager bootStrapManager;
    private BandanaManager bandanaManager;
    private SpaceDao spDao;
    
    //Following variables store user input, to display user input values if any input is incorrect! 
    private String users = null;
    private String groupsToAdd = null;
    private List selectedUserGroupsList = null;
    private String adminAction = null;

    private GroupManagementService groupManagementService;
    private CustomPermissionConfiguration customPermissionConfiguration;

    public CustomPermissionManagerAction()
	{
		spDao = (SpaceDao) ContainerManager.getComponent("spaceDao");
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

	public String execute() throws Exception
    {
		log.debug("CustomPermissionManagerAction - log - Inside execute...");

    	String loggedInUser = getRemoteUser().getName();
		String key = getKey();
    	//String userids = ServletActionContext.getRequest().getParameter("userList");
    	String adminAction = getAdminAction();

        List userIdList = StringUtil.convertDelimitedStringToCleanedLowercaseList(getUsers());
        List groupIdToAddList = StringUtil.convertDelimitedStringToCleanedLowercaseList(getGroupsToAdd());

        Map paramMap = ServletActionContext.getRequest().getParameterMap();
    	//Get list of user selected usergroups checkboxes
    	List groupList = retrieveListOfSelectedUserGroups(paramMap);

    	//Validate user input
        boolean isValid = validateInput(adminAction, paramMap, userIdList, groupIdToAddList);
    	if(!isValid)
        {
            log.debug("Input was invalid");
            return ERROR;
        }

    	String userGroupsValidationMessage = validateUserGroupWikiSpaceAssociation(groupList);
    	if(userGroupsValidationMessage != null)
    	{
            log.debug("There are no groups this user can currently administer. message=" + userGroupsValidationMessage);
            addFieldError("NotPermittedUserGroupsErrorMessage", userGroupsValidationMessage);
    		return ERROR;
    	}
    	
    	//Process user groups as per type of User Management Service used. It can be from CONFLUENCE or JIRA
    	String userManagementLocation = getUserManagerLocation();
    	if(userManagementLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE))
    	{
        	//Using Confluence for user management
            return manageUsersInConfluence( loggedInUser,
											key,
											userIdList,
											groupList,
                                            groupIdToAddList,
                                            adminAction);
    	}
    	//Using Jira for user management
    	else if(userManagementLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE))
    	{
        	//First generate a secret Id that we want to pass with data.
        	String secretId = getSecretId(loggedInUser);
        	if(secretId == null)
        	{
        		//Looks like there is some problem in getting id for given user. Is JiraDS configuration wrong ?
        		addActionError("Error! Couldn't get User details, Please verify Jira JNDI Datasource with Confluence Administrator.");
        		return ERROR;
        	}

            //call Jira RPC service
            callJiraRPCService( loggedInUser,
    							key,
    							userIdList,
    							groupList,
    							adminAction,
    							secretId);
    	}

       return super.execute();
    }

    public List getUsersGroupsAssociatedForSpace() {
        return this.getGroupManagementService().findGroups(this.getSpace());
    }

    //Helps to retrieve usergroups selected by User - removed "groups_" from selected checkbox name
    public String buildUserGroupsFromCheckboxName(String checkboxName)
    {
        String[] splitUpCheckboxName = checkboxName.split("_", 2);
    	return splitUpCheckboxName[1];
    }

    //Get the list of user groups that user has selected
    private List retrieveListOfSelectedUserGroups(Map paramMap)
    {
    	List selectedUserGroupsList = new ArrayList(4);
    	
    	//Get all groups that user has selected.
        for (Iterator iterator = paramMap.keySet().iterator(); iterator.hasNext();)
        {
            String paramKey = (String) iterator.next();
            if(paramKey != null && paramKey.startsWith("groups_"))
            {
            	selectedUserGroupsList.add(buildUserGroupsFromCheckboxName(paramKey));
            }
        }
        
    	this.selectedUserGroupsList = selectedUserGroupsList;
    	return selectedUserGroupsList;
    }
    
    //If input group name matches with user select group, then return true
    //This function will be useful to remember user selection of checkbox during displaying errors!
    public boolean isGroupSelected(String groupName)
    {
    	if( selectedUserGroupsList!=null && selectedUserGroupsList.contains(groupName))
    	{
    		return true;
    	}
    	
      return false;
    }

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
        if (GeneralUtil.isSuperUser(getRemoteUser()))
            return true;

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
   
        List permittedSpacesForUser = spDao.getPermittedSpacesForUser(user,permission);
        Collections.sort(permittedSpacesForUser, new SpaceComparator());

        return permittedSpacesForUser;
    }
    
    /*
     * If logged in user is Confluence Administrator, then get list of wiki spaces available in Confluence
     */
    public List getAllSpaces()
    {
        return spDao.findAllSorted("name");
    }

    public String getCollectionAsString(Collection c) {
        if ( c != null ) {
            StringBuffer sb = new StringBuffer();
            boolean needSeparator = false;
            for (Iterator iterator = c.iterator(); iterator.hasNext();) {
                if ( needSeparator ) {
                    sb.append( ", " );
                }
                sb.append( '\'' );
                sb.append( (String) iterator.next() );
                sb.append( '\'' );
                needSeparator = true;
            }
            return sb.toString();
        }
        return null;
    }

    //Get list of usergroups associated to given wiki space.
    public String validateUserGroupWikiSpaceAssociation(List selectedUserGroups)
    {
        String notAllowedGroups = null;

        if ( selectedUserGroups != null )
        {

            if (log.isDebugEnabled())
            {
                log.debug("validateUserGroupWikiSpaceAssociation() called with groups:" +
                        getCollectionAsString(selectedUserGroups));
            }

            List notAssociatedUserGroups = new ArrayList();

            //Set currentAssociatedUserGroupsSet = spacePermissionManager.getGroupsForPermissionType(SpacePermission.VIEWSPACE_PERMISSION, getSpace()).keySet();
            List currentAssociatedUserGroupsSet = getGroupManagementService().findGroups(getSpace());
            for (Iterator iterator = selectedUserGroups.iterator(); iterator.hasNext();)
            {
                String grpName = (String) iterator.next();

                //check if this group name is present in current Associated usergroups for this wiki space or not.
                if(!currentAssociatedUserGroupsSet.contains(userAccessor.getGroup(grpName)))
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Group '" + grpName + "' was not found in currentAssociatedUserGroupsSet: " + getCollectionAsString(currentAssociatedUserGroupsSet));
                    }

                    notAssociatedUserGroups.add(grpName);
                }
                else
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Can manage group '" + grpName + "'");
                    }
                }
            }

           if(notAssociatedUserGroups.size()> 0)
           {
               notAllowedGroups = "";
               for(Iterator itr= notAssociatedUserGroups.iterator(); itr.hasNext();)
               {
                   notAllowedGroups += ", " + (String)itr.next() ;
               }

               notAllowedGroups = "You are not authorized to modify usergroups - " + notAllowedGroups.replaceFirst(", ", "") ;
           }
        }
       
        return notAllowedGroups;
    }
    
    //This method will be used to create an user when Confluence is used for Managing Wiki Users
	private User createConfUser(String creationUserName, boolean isLDAPAvailable, String companyLDAPUrl, String companyLDAPBaseDN)
	{
		User vUser = null;
		LDAPUser lUser = null;
		
		log.debug("create a confluence user -> " + creationUserName);
		
		try {
			//if LDAP Lookup is available, get information from there.
			if(isLDAPAvailable)
			{
				//log.debug("LDAP Lookup available");
				//Get user details from LDAP.
				lUser = LDAPUtil.getLDAPUser(creationUserName,companyLDAPUrl,companyLDAPBaseDN);
				if(lUser != null)
				{
					vUser = userAccessor.addUser(creationUserName,creationUserName, lUser.getEmail(), lUser.getFullName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return vUser;
	}

    //This method will be used to create a group when Confluence is used for Managing Wiki Groups
	private Group createConfSpaceGroup(String creationGroupName)
	{
		Group vGroup = null;

		log.debug("create a confluence group -> " + creationGroupName);

		try {
			vGroup = userAccessor.addGroup(creationGroupName);
        } catch (Exception e) {
			e.printStackTrace();
		}

		return vGroup;
	}

    public String manageUsersInConfluence(String actionPerformerUser,
			 String spaceKey,
			 List userIdList,
			 List groupList,
             List groupIdToAddList,
             String adminAction)
	{
		//Result List. it Looks like we can't sent status as output object, rpc call doesn't support custom objects! Not sure....
		List resultList = new ArrayList();
		String opMessage = null;
		//Following vector holds all those userids, which are not in system and also we couldn't create them as LDAP doesn't have information about it. 
		String vNotCreatedUsers = "";
        String vNotCreatedGroups = "";
        String vGroupsNotMatched = "";
        //Following vector holds all groups to which we are not able to add users.
		String vNotUsedGroup = "";

		if (adminAction==null) {
            resultList.add("Please select an action.");
            setActionErrors(resultList);
            return ERROR;
        }
        else if(adminAction.equals("AddToGroups"))
        {
	        boolean isLDAPPresent = getIsLdapAuthUsed().equals(CustomPermissionConfigConstants.YES) ? true : false;
    		try
    		{
        		//Associate selected user-groups to all users.
        		for(Iterator itr = userIdList.iterator(); itr.hasNext();)
        		{
        			//First check if given user is present or not
        			String userid = (String) itr.next();
        			User currUser = userAccessor.getUser(userid);
        			if(currUser == null)
        			{
        				//create an user
        				//userid doesn't exists, if LDAP present then we will create User if it exists in LDAP.
        		        if(isLDAPPresent)
        		        {
        		        	//create an user.
        		        	currUser = createConfUser(userid,isLDAPPresent, getCompanyLDAPUrl(),getCompanyLDAPBaseDN());
        		        }

        		        //if user details not found in LDAP too, then retun userid in errorids
        		        if(currUser == null)
        		        {
    						//ok for some reasons we are unable to create user.
    						//Let's add it to our notCreatedUser List.
        		        	vNotCreatedUsers += ", " + userid;
    						//let's jump to next userid.
    						log.debug("User not found ...go to next user id");
    						continue;
        		        }
    					else
    					{
    						//Add this user to default group confluence-users
    						userAccessor.addMembership("confluence-users", userid);
    					}
        			}

        			//If user exists then associate him/her to all selected usergroups
        			if (currUser != null)
        			{
        	        	//Associate this user to all selected user-groups
        	        	for(Iterator iterator = groupList.iterator(); iterator.hasNext();)
        	        	{
        	        		userAccessor.addMembership((String)iterator.next(), userid);
        	        	}
        				
        			}
        		}
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			resultList.add("Error! - " + e.getMessage());
            	setActionErrors(resultList);
    			return ERROR;
    		}

        	//Set the Message for Add operation appropriately
			if(vNotCreatedUsers != "")
			{
				opMessage ="Some of the Users may not have been added to System." ;
				
				String LDAPErrorMessage = "<br>Either following are invalid userids Or there is some issue with LDAP configuration.<br> Please verify with Confluence Administrator.";
				//If LDAP is used for creating users, then it might be LDAP settings issue. hence display error message accordingly
				if(isLDAPPresent)
				{
					opMessage += LDAPErrorMessage;
				}
				
				opMessage = "<font color=\"red\">" + opMessage + "</font>";
				
				vNotCreatedUsers = "<font color=\"red\">List of not added Userids: " + vNotCreatedUsers.replaceFirst(",", "") + "</font>";
			}
			else
			{
				opMessage = "<font color=\"green\">All users are added successfully!</font>";
			}
			
			resultList.add(opMessage);
			resultList.add(vNotCreatedUsers);
			
        	setActionMessages(resultList);
        	flushUserInputs();
        	return SUCCESS;
        }
        else if(adminAction.equals("RemoveFromGroups"))
        {
        	try{
	        	//Remove User from all mentioned groups         	
        		for(Iterator itr = userIdList.iterator(); itr.hasNext();)
	    		{
        			String userid = (String) itr.next();
		        	for(Iterator iterator = groupList.iterator(); iterator.hasNext();)
		        	{
		        		userAccessor.removeMembership((String)iterator.next(), userid);
		        	}
	    		}
        	}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			resultList.add("Error! - " + e.getMessage());
    			return ERROR;
    		}
    		
			opMessage = "<font color=\"green\">All users are successfully removed from selected groups!</font>";
			resultList.add(opMessage);
        	setActionMessages(resultList);
        	flushUserInputs();
        	
        	return SUCCESS;
        }
        else if(adminAction.equals("AddGroups"))
        {
	        try
    		{
        		//Add groups

                for(Iterator itr = groupIdToAddList.iterator(); itr.hasNext();)
        		{
        			//First check if given group is present or not
        			String groupid = (String) itr.next();
        			Group currGroup = userAccessor.getGroup(groupid);
        			if(currGroup == null)
        			{
        				//create a group only if it matches pattern
                        Pattern pat = GroupMatchingUtil.createGroupMatchingPattern(bandanaManager, getKey());
                        boolean isPatternMatch = GroupMatchingUtil.doesGroupMatchPattern(groupid, pat);

                        if (isPatternMatch) {

                            currGroup = userAccessor.addGroup(groupid);
                            if(currGroup == null)
                            {
                                //ok for some reasons we are unable to create user.
                                //Let's add it to our notCreatedUser List.
                                vNotCreatedGroups += ", " + groupid;
                                //let's jump to next userid.
                                log.debug("Group '" + groupid + "' not created");
                            }
                        }
                        else {
                            if (vGroupsNotMatched.length()>0) {
                                vGroupsNotMatched += ", ";
                            }

                            vGroupsNotMatched += groupid;
                            log.debug("Group '" + groupid + "' not created because it didn't match pattern");
                        }
                    }

        			//If group exists then set all required permissions
        			if (currGroup != null)
        			{
        	        	SpacePermission perm = new SpacePermission(SpacePermission.VIEWSPACE_PERMISSION,
                                getSpace(), currGroup.getName());
                        getSpace().addPermission(perm);

        			}
        		}
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			resultList.add("Error! - " + e.getMessage());
            	setActionErrors(resultList);
    			return ERROR;
    		}

        	//Set the Message for Add operation appropriately
			if(!("".equals(vNotCreatedUsers)) || !("".equals(vGroupsNotMatched)))
			{
				opMessage = "Some of the Groups may not have been added to System.";
                resultList.add("<font color=\"red\">" + opMessage + "</font>");

                if (!("".equals(vNotCreatedUsers))) {
                    resultList.add("<font color=\"red\">List of not added Groupids: " + vNotCreatedGroups.replaceFirst(",", "") + "</font>");
                }
                if (!("".equals(vGroupsNotMatched))) {
                    resultList.add("<font color=\"red\">List of not added Groupids that weren't added because they didn't match: " + vGroupsNotMatched + "</font>");
                }
            }
			else
			{
				opMessage = "All groups are added successfully!";
                resultList.add("<font color=\"green\">" + opMessage + "</font>");
			}


			setActionMessages(resultList);
        	flushUserInputs();
        	return SUCCESS;
        }
        else if(adminAction.equals("RemoveGroups"))
        {
        	String groupsNotDeleted = "";
            try{
	        	//Remove Selected Groups
        		for(Iterator iterator = groupList.iterator(); iterator.hasNext();)
                {
                    String grpName = (String)iterator.next();
                    Pattern pat = GroupMatchingUtil.createGroupMatchingPattern(bandanaManager, getKey());
                    boolean isPatternMatch = GroupMatchingUtil.doesGroupMatchPattern(grpName, pat);

                    if (!grpName.startsWith("conf") && isPatternMatch) {
                        Group group = userAccessor.getGroup(grpName);
                        if (group!=null) {
                            userAccessor.removeGroup(group);
                        }
                    }
                    else {
                        log.debug("Not deleting group '" + grpName + "', as either it started with 'conf' or didn't match pattern " + GroupMatchingUtil.getUserGroupsMatchingPattern(bandanaManager));

                        if (!("".equals(groupsNotDeleted))) {
                            groupsNotDeleted += ",";
                        }

                        groupsNotDeleted += grpName;
                    }
                }
        	}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			resultList.add("Error! - " + e.getMessage());
    			return ERROR;
    		}

            if ("".equals(groupsNotDeleted)) {
                opMessage = "The selected groups were successfully removed!";
                resultList.add("<font color=\"green\">" + opMessage + "</font>");
            }
            else {
                opMessage = "The following selected groups were not deleted, either because they started with 'conf' or didn't match pattern " + GroupMatchingUtil.getUserGroupsMatchingPattern(bandanaManager) + ": " + groupsNotDeleted;
                resultList.add("<font color=\"red\">" + opMessage + "</font>");
            }

            setActionMessages(resultList);
        	flushUserInputs();

        	return SUCCESS;
        }
		
        return SUCCESS;
	}
	
    //call Jira RPC Service
    public void callJiraRPCService(String actionPerformerUser,
    								 String spaceKey,
    								 List userIdList,
    								 List groupList,
    								 String adminAction,
    								 String secretId)
    {
        List resultVec = null;
        String errorMsg = null; 
        
        String tempUserIDs = "";
		for(Iterator itr = userIdList.iterator(); itr.hasNext();)
		{
			tempUserIDs +=(String) itr.next() + ",";
		}

        String groupNames = "";
		for(Iterator itr = groupList.iterator(); itr.hasNext();)
		{
			groupNames +=(String) itr.next()+ ",";
		}
		
		log.debug("Space Admin - " + actionPerformerUser);
		log.debug("Space Key - " + spaceKey);
		log.debug("Input Users - " + tempUserIDs);
		log.debug("Input Group names - " + groupNames);
        log.debug("AdminAction - " + adminAction);
        log.debug("Secretid - " + secretId );
        log.debug("getJiraJNDILookupKey - " + getJiraJNDILookupKey());
        log.debug("getLdapAuthKey() - " + getIsLdapAuthUsed() );
        log.debug("getJiraUrl() - " + getJiraUrl());
        log.debug("getCompanyLDAPUrl - " + getCompanyLDAPUrl());
        log.debug("getCompanyLDAPBaseDN - " + getCompanyLDAPBaseDN());
                		
        Boolean isLDAPLookupAvailable = new Boolean(getIsLdapAuthUsed().equals(CustomPermissionConfigConstants.YES) ? true : false);
        log.debug( "isLDAPLookupAvailable - " + isLDAPLookupAvailable);

        //Ok time to call Jira RPC plugin as we have all data that needs to be processed.
        try
        {
            // Initialise RPC Client
            XmlRpcClient rpcClient = new XmlRpcClient(getJiraUrl() + RPC_PATH);
            log.debug( " Making call to Jira RPC Client");

            // Login and retrieve logon token
            Vector rpcParams = new Vector(10);
            //Enter User who is sending this request to RPC call.
            rpcParams.add(actionPerformerUser);
            //Space Name.
            rpcParams.add(spaceKey);
            //Get the list of user ids on which we want to act upon.
            rpcParams.add(userIdList);
            //Groups to which users need to associate.
            rpcParams.add(groupList);
            //Jira Datasource
            rpcParams.add(getJiraJNDILookupKey());
            //Confluence Url
            rpcParams.add(getConfluenceUrl());
            //Secret Id
            rpcParams.add(secretId);
            
            if(adminAction.equalsIgnoreCase("AddToGroups"))
            {
                log.debug( " Adding users to group");

                //If LDAP Lookup available, then pass that info for user creation
                //Note if isLDAPLookupAvailable is false, then LDAPurl, BaseDN should contain empty strings "" else xml-rpc will throw error!
                rpcParams.add(isLDAPLookupAvailable);
                rpcParams.add(getCompanyLDAPUrl());
                rpcParams.add(getCompanyLDAPBaseDN());

	            resultVec = (Vector) rpcClient.execute("delegateusermgmt.addUsersToGroups", rpcParams);
	            log.debug("Result is - " + resultVec.get(0));
            }
            else if(adminAction.equalsIgnoreCase("RemoveFromGroups"))
            {
                log.debug( " Removing users from group");
	            resultVec = (Vector)rpcClient.execute("delegateusermgmt.removeUsersFromGroups", rpcParams);
            }
            
            //depending upon result of action, display messages.
            if( ((String)resultVec.get(0)).startsWith("Error"))
            {
            	setActionErrors(resultVec);
            }
            else
            {
            	setActionMessages(resultVec);
            	//Since action is carried successfully, clean user input
            	flushUserInputs();
            }
            return;
            
        }
        catch (Exception e)
        {
        	errorMsg = e.getMessage();
            e.printStackTrace();
        }
        
        if(errorMsg != null)
        {
        	resultVec = new Vector();
        	resultVec.add(errorMsg + ". Please verify this error with confluence administrator.");
            setActionErrors(resultVec);
        }
    }

    //Getter / Setters for users input text box - Helps to remember values during error display
    public String getUsers()
    {
    	return users;
    }

    public void setUsers(String users)
    {
    	this.users = users;
    }

    //Getter / Setters for groupsToAdd input text box - Helps to remember values during error display
    public String getGroupsToAdd()
    {
    	return groupsToAdd;
    }

    public void setGroupsToAdd(String groupsToAdd)
    {
    	this.groupsToAdd = groupsToAdd;
    }

    //Getter / Setters for adminAction radio button - Helps to remember values during error display
    public String getAdminAction()
    {
    	return adminAction;
    }

    public void setAdminAction(String adminAction)
    {
    	this.adminAction = adminAction;
    }

	//If admin action has been carried successfully, then clean user input as we don't want to remember it!
	//This method should be called whenever we return SUCCESS
    public void flushUserInputs()
    {
    	//No need to remember User Input
    	setUsers(null);
        setGroupsToAdd(null);
        setAdminAction(null);
    	this.selectedUserGroupsList = null;    	
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
	
	public String getConfluenceUrl(){
    	//Get base URL for confluence installation
        bootStrapManager = (BootstrapManager) ContainerManager.getInstance().getContainerContext().getComponent("bootstrapManager");
        return bootStrapManager.getDomainName();
	}
    
	//If Any of configuration parameter is not set then display Plugin Configurtion Screen!
    public boolean getIsPluginConfigurationDone()
    {
    	boolean flag = false;
    	
    	/*    	
    	 * 	Following is very long validation which checks if all required variables are set or not.
    	 *	To make life easier, validation is done on each field and used in following section.
    	 */
    	boolean isJiraSettingDone = false; //This validation is needed only if User is using "JIRA" for user management.
    	boolean isMaxUserLimitSet = false;
    	boolean isLDAPConfigDone = false;
    	boolean isPluginOkToRun = false; //This will be true only we need to get plugin offilne by displaying downtime message.
    	
    	String userManagementLocation = getUserManagerLocation();
    	if( (userManagementLocation!= null)&& (userManagementLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE)) )
    	{
    		//if user is using JIRA for User Management then we need to Jira Information
        	if( (getJiraUrl()!= null) ||  (getJiraJNDILookupKey()!=null))
        	{
        		log.debug("Jira Setting done - Jira URL - " + getJiraUrl() + " Jira JNDI Lookup - " + getJiraJNDILookupKey());
        		isJiraSettingDone = true;
        	}
    	}
    	else if( (userManagementLocation!= null)&& (userManagementLocation.equals(CustomPermissionConfigConstants.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE)) )
    	{
    		//Since we are using Confluence, no need for Jira information. We will set flag as true
    		isJiraSettingDone = true;
    	}
 

    	if( getMaxUserIDsLimit() != null)
    	{
    		log.debug("Input User IDs Limit Setting done. value - " + getMaxUserIDsLimit());
    		isMaxUserLimitSet = true;
    	}

    	String isLDAPAuthenticationAvailable = getIsLdapAuthUsed();
    	if( (isLDAPAuthenticationAvailable != null) && (isLDAPAuthenticationAvailable.equalsIgnoreCase("no")) )
    	{
    		log.debug("LDAP setting not needed");
    		isLDAPConfigDone = true;
    	}
    	else if ( (isLDAPAuthenticationAvailable != null) && (isLDAPAuthenticationAvailable.equalsIgnoreCase("yes")) )
    	{
    		if((getCompanyLDAPUrl() != null) && (getCompanyLDAPBaseDN()!= null))
    		{
        		log.debug("LDAP setting done LDAP Url - " + getCompanyLDAPUrl() + "  BaseDN - " + getCompanyLDAPBaseDN());
    			isLDAPConfigDone = true;
    		}
    	}
    		
    	//If downtime flag is false, then plugin is ok to use by users!
    	if(getIsPluginDown() == null || getIsPluginDown().equalsIgnoreCase("no"))
    	{
    		log.debug("Plugin is ok to run");
    		isPluginOkToRun = true;
    	}
    	
    	//if all above flags are true, then set flag as true!
    	if(	isJiraSettingDone
    			&& isMaxUserLimitSet 
    			&& isLDAPConfigDone
    			&& isPluginOkToRun)
    	{
    		flag = true;
    	}
    	
    	return flag;
    }

	//For requester Space Administrator, get the id value from jira schema
    //This value will be passed along with data on which we want to act.
    //If this id matches with one that is reterived there, then this user request is really coming from Authentic Space Administrator related to given space.
    public String getSecretId(String requesterUserId)
    {
    	String secretId = null;
    	
        Connection connection = null;
        PreparedStatement statement = null;
		ResultSet resultSet = null;
        DataSource ds = null;
        String jiraJNDI = "java:comp/env/" + getJiraJNDILookupKey() ;
        InitialContext ctx = null;
        
		try
		{
			ctx = new InitialContext();
            try {
                ds = (DataSource) ctx.lookup(jiraJNDI);
			}
            catch (NameNotFoundException exception) {
                log.debug("dataSource: " + jiraJNDI + " not found.");
                exception.printStackTrace();
                //not able to connect to jira database.
               return secretId;
            }
            
            connection = ds.getConnection();
            String sql = "select id from userbase where username = ?";
			statement = null;

			try {
				statement = connection.prepareStatement(sql);
				statement.setString(1,requesterUserId);
                resultSet = statement.executeQuery();
                if(resultSet != null && resultSet.next())
                {
                	secretId = resultSet.getString(1);
                }
            }
            catch (SQLException sqlException) { 
            	// this case shouldn't come, as requester User's verification is already done successfully.
            	sqlException.printStackTrace();
                //not able to connect to jira database.
            }
		}
    	catch(Exception exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			try
			{
				if (resultSet != null)
				{
					resultSet.close();
				}
				
				if (statement != null)
				{
					statement.close();
				}
				
				if (connection != null)
				{
					connection.close();
				}
			}
			catch(Exception discard) {}
		}
	 
		//log.debug("Secret id for user " + requesterUserId + " is " + secretId);
		
	  return secretId;
    	
    }

    private boolean isGroupSelected(Map paramMap) {
        boolean result = false;
        for (Iterator iterator = paramMap.keySet().iterator(); iterator.hasNext();)
        {
            String paramKey = (String) iterator.next();
            if(paramKey != null && paramKey.startsWith("groups_"))
            {
                result = true;
                break;
            }
        }
        return result;
    }

    //Validate user input. return false if data invalid.
    public boolean validateInput(String adminAction, Map paramMap, List userIdList, List groupToAddIdList )
    {
        log.debug("Validating adminAction=" + adminAction +
                ", paramMap=" + paramMap +
                ", userIdList=" + StringUtil.convertCollectionToCommaDelimitedString(userIdList) +
                ", groupToAddIdList=" + StringUtil.convertCollectionToCommaDelimitedString(groupToAddIdList));

        boolean isValid = true; //By default validation will be successful

        //Will use default of 20 if we don't validate max UserIDLimit during configuration or user has changed value by modifying xml file (confluence-home/config/confluence-global.bandana.xml).
        int maxUserIDLimit = ConfigUtil.getIntOrUseDefaultIfNullOrTrimmedValueIsEmptyOrNotAnInteger("maxUserIDLimit", getMaxUserIDsLimit(), 20);

        if(adminAction == null)
    	{
    		//user has not selected which action to perform.
            addFieldError("adminAction", "Please select an action.");
            isValid = false;
        }
        else if(adminAction.equals("AddToGroups") || adminAction.equals("RemoveFromGroups")) {

            if(!isGroupSelected(paramMap))
            {
                addFieldError("groups_", "Please select at least one group.");
                isValid = false;
            }
            else if(userIdList == null || userIdList.size()==0)
            {
                addFieldError("users", "Please enter list of usernames to act on.");
                isValid = false;
            }
            else
            {
                if( ListUtil.isListSizeOverMaxNum( userIdList, maxUserIDLimit ) )
                {
                    addFieldError("users", "Only "+ maxUserIDLimit + " users will be processed at a time.");
                    isValid = false;
                }
            }
        }
        else if(adminAction.equals("AddGroups"))
        {
            if(groupToAddIdList == null || groupToAddIdList.size()==0)
            {
                addFieldError("groupsToAdd", "Please enter list of groups to add.");
                isValid = false;
            }
            else
            {
                if( ListUtil.isListSizeOverMaxNum( userIdList, maxUserIDLimit ) )
                {
                    addFieldError("groupsToAdd", "Only "+ maxUserIDLimit + " groups will be processed at a time.");
                    isValid = false;
                }
            }
        }
        else if(adminAction.equals("RemoveGroups"))
        {
            if(!isGroupSelected(paramMap))
            {
                addFieldError("groups_", "Please select at least one group.");
                isValid = false;
            }
        }
        else {
    		addFieldError("adminAction", "'" +adminAction + "' is not a valid action!");
            isValid = false;
        }

        return isValid;
    }

    //Get total user count for given user group
    public int findUserCountForUserGroup(String grpName)
    {
    	return PagerUtils.count(userAccessor.getMemberNames(userAccessor.getGroup(grpName)));
    }

    public GroupManagementService getGroupManagementService() {
        return groupManagementService;
    }

    public void setGroupManagementService(GroupManagementService groupManagementService) {
        this.groupManagementService = groupManagementService;
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }

    public String getActionName(String fullClassName)
    {
    	return "Custom Space Usergroups Manager";
    }
  }