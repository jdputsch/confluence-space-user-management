package raju.kadam.confluence.permissionmgmt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import org.apache.xmlrpc.XmlRpcException;
import javax.sql.DataSource;

import org.apache.xmlrpc.XmlRpcClient;

import raju.kadam.util.LDAP.*;

import bucket.container.ContainerManager;

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
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.webwork.ServletActionContext;
import java.util.regex.*;

import java.io.IOException;
import java.net.MalformedURLException;

public class CustomPermissionManagerAction extends AbstractSpaceAction implements SpaceAdministrative
{
    public static final String RPC_PATH  = "/rpc/xmlrpc";

    private BootstrapManager bootStrapManager;
    private BandanaManager bandanaManager;
    private SpaceDao spDao;
    //Following variables store user input, to display user input values if any input is incorrect! 
    private String userList = null;
    private Vector selectedUserGroupsList = null;
    private String adminAction = null;

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
		
    	//String key = ServletActionContext.getRequest().getParameter("key");
		String key = getKey();
    	//String adminAction = ServletActionContext.getRequest().getParameter("adminAction");
    	String actionDirective = getAdminAction();
    	//String userids = ServletActionContext.getRequest().getParameter("userList");
    	String userids = getUserList();

    	Map paramMap = ServletActionContext.getRequest().getParameterMap();
    	//Get list of user selected usergroups checkboxes
    	Vector groupList = retrieveListOfSelectedUserGroups(paramMap);

    	//Validate user input
    	ArrayList resultList = validateInput(adminAction, paramMap, userids);
    	boolean validationFlag = ((Boolean)resultList.get(0)).booleanValue();
    	if(!validationFlag) return ERROR;

    	//Since validation is successful, 2nd element in resultList will contain list of userids to process
    	Vector vUserIDsVec = (Vector) resultList.get(1);

    	//Process user groups as per type of User Management Service used. It can be from CONFLUENCE or JIRA
    	String USER_MANGEMENT_APP = getUserManagerLocation(); 
    	if(USER_MANGEMENT_APP.equals(CustomPermissionConfigAction.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE))
    	{
        	//Using Confluence for user management
            return manageUsersInConfluence( loggedInUser,
											key,
											vUserIDsVec,
											groupList,
											actionDirective);
    	}
    	//Using Jira for user management
    	else if(USER_MANGEMENT_APP.equals(CustomPermissionConfigAction.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE))
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
    							vUserIDsVec,
    							groupList,
    							adminAction,
    							secretId);
    	}

       return super.execute();
    }

	//Helps to reterive usergroups selected by User - removed "groups_" from selected checkbox name
    public String buildUserGroupsFromCheckboxName(String checkboxName)
    {
        String[] splitUpCheckboxName = checkboxName.split("_", 2);
    	return splitUpCheckboxName[1];
    }

    //Get the list of user groups that user has selected
    private Vector retrieveListOfSelectedUserGroups(Map paramMap)
    {
    	Vector selectedUserGroupsList = new Vector(4);
    	
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
    public boolean isGroupSelected(String vGroupName)
    {
    	if( selectedUserGroupsList!=null && selectedUserGroupsList.contains(vGroupName))
    	{
    		return true;
    	}
    	
      return false;
    }
    
    //Get all users groups which Space Admins can manage 
    public List getUsersGroupsAssociatedForSpace()
    {
    	ArrayList notAllowedUserGroups = new ArrayList();
    	notAllowedUserGroups.add("confluence-administrators");

    	Pattern pat = null;
    	Matcher matcher = null;
    	
        String groupPattern = getUserGroupsMatchingPattern();
        if(groupPattern == null || (groupPattern.trim().equals("")))
        {
        	//This will only happen if we don't validate matching pattern during configuration.
        	groupPattern = "SPACEKEY-.*";
        }
        
    	//If spacekey is present in groupPattern then before compiling it replace it with current space key
    	if(groupPattern.indexOf("SPACEKEY")!= -1)
    	{
    		//Replace String "SPACEKEY" with input Space Key. 
    		groupPattern = groupPattern.replaceFirst("SPACEKEY", getKey());
    	}

    	log.debug("group pattern -> " + groupPattern);
    	
        pat=Pattern.compile(groupPattern);
        
        List result = new ArrayList();

        //VIEWSPACE_PERMISSION is basic permission that every user group can have.
        for (Iterator iterator = spacePermissionManager.getGroupsForPermissionType(SpacePermission.VIEWSPACE_PERMISSION, getSpace()).keySet().iterator(); iterator.hasNext();)
        {
        	String grpName = (String) iterator.next();
        	matcher = pat.matcher(grpName);
        	
        	//If notAllowedUserGroups doesn't contain this group name 
        	//and group name matches the pattern, then only add this user-group for display.
    		//log.debug("Selected Groups .....");
        	if( (!notAllowedUserGroups.contains(grpName)) && (matcher.matches()))
        	{
        		//log.debug("group - " +	 grpName);
        		result.add(userAccessor.getGroup(grpName));
        	}
    		//log.debug("-------End of Groups---------");
    		
        }

        Collections.sort(result, new Comparator()
        {
            public int compare(Object o, Object o1)
            {
                return ((Group) o).getName().compareToIgnoreCase(((Group) o1).getName());
            }
        });

        return result;
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

    public String manageUsersInConfluence(String actionPerformerUser,
			 String spaceKey,
			 Vector iUserIDsVec,
			 Vector iGroupList,
			 String actionDirective)
	{
		//Result Vector. it Looks like we can't sent status as output object, rpc call doesn't support custom objects! Not sure....
		Vector resultVector = new Vector();
		String opMessage = null;
		//Following vector holds all those userids, which are not in system and also we couldn't create them as LDAP doesn't have information about it. 
		String vNotCreatedUsers = "";
		//Following vector holds all groups to which we are not able to add users.
		String vNotUsedGroup = "";

		if(actionDirective.equals("AddToGroups"))
        {
	        boolean isLDAPPresent = getIsLdapAuthUsed().equals(CustomPermissionConfigAction.DELEGATE_USER_LDAP_AUTH_KEY_YES_VALUE) ? true : false;
    		try
    		{
        		//Associate selected user-groups to all users.
        		for(Iterator itr = iUserIDsVec.iterator(); itr.hasNext();)
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
        	        	for(Iterator iterator = iGroupList.iterator(); iterator.hasNext();)
        	        	{
        	        		userAccessor.addMembership((String)iterator.next(), userid);
        	        	}
        				
        			}
        		}
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			resultVector.add("Error! - " + e.getMessage());
            	setActionErrors(resultVector);
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
			
			resultVector.add(opMessage);
			resultVector.add(vNotCreatedUsers);
			
        	setActionMessages(resultVector);
        	flushUserInputs();
        	return SUCCESS;
        }
        else if(adminAction.equals("RemoveFromGroups"))
        {
        	try{
	        	//Remove User from all mentioned groups         	
        		for(Iterator itr = iUserIDsVec.iterator(); itr.hasNext();)
	    		{
        			String userid = (String) itr.next();
		        	for(Iterator iterator = iGroupList.iterator(); iterator.hasNext();)
		        	{
		        		userAccessor.removeMembership((String)iterator.next(), userid);
		        	}
	    		}
        	}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			resultVector.add("Error! - " + e.getMessage());
    			return ERROR;
    		}
    		
			opMessage = "<font color=\"green\">All users are successfully removed from selected groups!</font>";
			resultVector.add(opMessage);
        	setActionMessages(resultVector);
        	flushUserInputs();
        	
        	return SUCCESS;
        }
		
		return SUCCESS;
	}
	
    //call Jira RPC Service
    public void callJiraRPCService(String actionPerformerUser,
    								 String spaceKey,
    								 Vector iUserIDsVec,
    								 Vector iGroupList,
    								 String actionDirective,
    								 String secretId)
    {
        Vector resultVec = null;
        String errorMsg = null; 
        
        String tempUserIDs = "";
		for(Iterator itr = iUserIDsVec.iterator(); itr.hasNext();)
		{
			tempUserIDs +=(String) itr.next() + ",";
		}

        String groupNames = "";
		for(Iterator itr = iGroupList.iterator(); itr.hasNext();)
		{
			groupNames +=(String) itr.next()+ ",";
		}
		
		log.debug("Space Admin - " + actionPerformerUser);
		log.debug("Space Key - " + spaceKey);
		log.debug("Input Users - " + tempUserIDs);
		log.debug("Input Group names - " + groupNames);
        log.debug("ActionDirective - " + actionDirective);
        log.debug("Secretid - " + secretId );
        log.debug("getJiraJNDILookupKey - " + getJiraJNDILookupKey());
        log.debug("getLdapAuthKey() - " + getIsLdapAuthUsed() );
        log.debug("getJiraUrl() - " + getJiraUrl());
        log.debug("getCompanyLDAPUrl - " + getCompanyLDAPUrl());
        log.debug("getCompanyLDAPBaseDN - " + getCompanyLDAPBaseDN());
                		
        Boolean isLDAPLookupAvailable = new Boolean(getIsLdapAuthUsed().equals(CustomPermissionConfigAction.DELEGATE_USER_LDAP_AUTH_KEY_YES_VALUE) ? true : false);
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
            rpcParams.add(iUserIDsVec);
            //Groups to which users need to associate.
            rpcParams.add(iGroupList);
            //Jira Datasource
            rpcParams.add(getJiraJNDILookupKey());
            //Confluence Url
            rpcParams.add(getConfluenceUrl());
            //Secret Id
            rpcParams.add(secretId);
            
            if(actionDirective.equalsIgnoreCase("AddToGroups"))
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
            else if(actionDirective.equalsIgnoreCase("RemoveFromGroups"))
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

    //Getter / Setters for userList input text box - Helps to remember values during error display
    public String getUserList()
    {
    	return userList;
    }

    public void setUserList(String userList)
    {
    	this.userList = userList;
    }

    //Getter / Setters for userList adminAction radio button - Helps to remember values during error display
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
    	userList = null;
    	adminAction = null;
    	selectedUserGroupsList = null;    	
    }
    
	public String getUserManagerLocation() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigAction.DELEGATE_USER_USER_MANAGER_LOCATION);
	}
    
	public String getJiraUrl() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigAction.DELEGATE_USER_MGMT_JIRA_URL);
	}
   
	public String getIsLdapAuthUsed()
    {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigAction.DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY);
    }
	
    public String getJiraJNDILookupKey()
    {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigAction.DELEGATE_USER_MGMT_JIRA_JNDI_KEY);
    }

	public String getCompanyLDAPBaseDN() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigAction.DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY);
	}

	public String getCompanyLDAPUrl() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigAction.DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY);
	}
	
	//Get the count which indicates total no. of userids that can be processed at a time.
	public String getMaxUserIDsLimit() {
        return ((String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigAction.DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT));
	}

	//Get Pattern to display all groups 
	public String getUserGroupsMatchingPattern() {
        return ((String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigAction.DELEGATE_USER_MGMT_USERGROUPS_MATCHING_PATTERN));
	}
	
	public String getIsPluginDown() {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigAction.DELEGATE_USER_MGMT_PLUGIN_STATUS);
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
    	if( (userManagementLocation!= null)&& (userManagementLocation.equals(CustomPermissionConfigAction.DELEGATE_USER_MANAGER_LOCATION_JIRA_VALUE)) )
    	{
    		//if user is using JIRA for User Management then we need to Jira Information
        	if( (getJiraUrl()!= null) ||  (getJiraJNDILookupKey()!=null))
        	{
        		log.debug("Jira Setting done - Jira URL - " + getJiraUrl() + " Jira JNDI Lookup - " + getJiraJNDILookupKey());
        		isJiraSettingDone = true;
        	}
    	}
    	else if( (userManagementLocation!= null)&& (userManagementLocation.equals(CustomPermissionConfigAction.DELEGATE_USER_MANAGER_LOCATION_CONFLUENCE_VALUE)) )
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

    //Validate user input. reurn ERROR if incorrect data sent.
    public ArrayList validateInput(String adminAction, Map paramMap, String userids )
    {
    	Vector vUserIDsVec = null;
    	Boolean validationFlag = new Boolean(true); //By default validation will be successful
    	ArrayList resultList = new ArrayList();
    	
    	if(adminAction == null)
    	{
    		//user has not selected which action to perform.
            addFieldError("adminAction", "Please select a action to perform!");
    	}
    	
    	boolean isGroupSelected = false;
        for (Iterator iterator = paramMap.keySet().iterator(); iterator.hasNext();)
        {
            String paramKey = (String) iterator.next();
            if(paramKey != null && paramKey.startsWith("groups_"))
            {
            	isGroupSelected = true;
            	break;
            }
        }
        
        if(!isGroupSelected)
        {
            addFieldError("groups_", "Please select a user group.");
        }
        
    	boolean userInputErrorFlag = false;
    	if(userids == null || userids.equals(""))
    	{
            addFieldError("userList", "Please enter list of usernames to act on");
            userInputErrorFlag = true;
    	}

    	boolean overlimitUserIdsInputFlag = false;
    	//User has enter usernames, check if they are not more than 50.
    	if (!userInputErrorFlag)
    	{
			//Userids should be lowercase.
	    	userids = userids.toLowerCase().trim();
			userids = userids.replaceAll("[<>/]", "");
			//Get list of all userids in string array.
			String[] userIDArray = userids.split("[:;,]");
			
			int maxUserIDLimit = 20;
			try
			{
				maxUserIDLimit = Integer.parseInt(getMaxUserIDsLimit());
			}
			catch(Exception e)
			{
				//This will happen only if we don't validate max UserIDLimit during configuration or user has changed value by modifying xml file (confluence-home/config/confluence-global.bandana.xml).
				maxUserIDLimit = 20;
			}
			
			if( userIDArray.length > maxUserIDLimit)
			{
				overlimitUserIdsInputFlag = true;
	            addFieldError("userList", "Only "+ maxUserIDLimit + " userids will be processed at a time");
				//return ERROR;
			}
			else
			{
				//set input for processing.
				vUserIDsVec = new Vector(Arrays.asList(userIDArray));
			}
			
    	}
        
        if((adminAction == null)||(!isGroupSelected) || (userInputErrorFlag) || (overlimitUserIdsInputFlag))
        {
        	validationFlag = new Boolean(false);
        }

       resultList.add(validationFlag);
       resultList.add(vUserIDsVec);
       
       return resultList;
    }
    
    public String getActionName(String fullClassName)
    {
    	return "Custom Space Usergroups Manager";
    }
    
}