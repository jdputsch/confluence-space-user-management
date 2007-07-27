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

import bucket.core.actions.PagerPaginationSupport;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.actions.SpaceAdministrative;
import com.atlassian.confluence.spaces.persistence.dao.SpaceDao;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.confluence.util.SpaceComparator;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.User;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import com.opensymphony.webwork.ServletActionContext;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.config.ConfigValidationResponse;
import raju.kadam.confluence.permissionmgmt.service.*;
import raju.kadam.confluence.permissionmgmt.service.vo.*;
import raju.kadam.confluence.permissionmgmt.util.ConfluenceUtil;
import raju.kadam.confluence.permissionmgmt.util.GroupNameUtil;
import raju.kadam.confluence.permissionmgmt.util.GroupUtil;
import raju.kadam.confluence.permissionmgmt.util.PagerPaginationSupportUtil;
import raju.kadam.util.ConfigUtil;
import raju.kadam.util.HtmlFormUtil;
import raju.kadam.util.ListUtil;
import raju.kadam.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.URL;
import java.util.*;

/**
 * 
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class CustomPermissionManagerAction extends AbstractPagerPaginationSupportCachingSpaceAction implements SpaceAdministrative
{
    private BandanaManager bandanaManager;
    private SpaceDao spaceDao;
    private CustomPermissionServiceManager customPermissionServiceManager;
    private CustomPermissionConfiguration customPermissionConfiguration;
    private String selectedGroup;
    private String userSearch;
    private boolean userSearchFormFilled;
    private AdvancedUserQuery advancedUserQuery;
    private String bulkEdit;
    private SettingsManager settingsManager;
    private String pagerAction;

    public static final String REDIRECT_PARAMNAME = "redirect";
    public static final String ACTION_ADD_GROUPS = "addGroups";
    public static final String ACTION_REMOVE_GROUPS = "removeGroups";
    public static final String ACTION_ADD_USERS_TO_GROUPS = "addUsersToGroups";
    public static final String ACTION_REMOVE_USERS_FROM_GROUPS = "removeUsersFromGroups";

    public CustomPermissionManagerAction()
	{
		log.debug("CustomPermissionManagerAction instance created");
    }
    
    public void setBandanaManager(BandanaManager bandanaManager)
    {
        this.bandanaManager = bandanaManager;
    }
    
	public String doDefault() throws Exception
    {
		//This method will be called very first time when user accesses .../custompermissionsmanage.action?key=<SPACEKEY>
		log.debug("CustomPermissionManagerAction - log - Inside doDefault ..");
        return execute();
    }

    private int getRowsPerPage() {
        return PagerPaginationSupport.DEFAULT_COUNT_ON_EACH_PAGE;
    }

    private String getUrlDecodedCleanedTrimmedParameterValue(Map paramMap, String param) {
        String result = null;
        String value = getRawParameterValue(paramMap, param);
        if (value!=null) {
            value = StringUtil.clean(value.trim());
            if (!StringUtil.isNullOrEmpty(value)) {
                try {
                    result = URLDecoder.decode(value, "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    log.error("request parameter '" + param + "' had cleaned up value '" + value + "' which could not be URL decoded", e);
                }
            }
        }
        return result;
    }

    private String getRawParameterValue(Map paramMap, String param) {
        String[] values = (String[])paramMap.get(param);
        if ( values != null && values.length > 0 ) {
            return values[0];
        }
        return null;
    }

    private List getUrlDecodedCleanedTrimmedParameterValueList(Map paramMap, String param) {
        List result = null;
        String decodedValue = getUrlDecodedCleanedTrimmedParameterValue(paramMap, param);
        if (decodedValue!=null) {
            result = StringUtil.getCleanedListFromDelimitedValueString(decodedValue);
        }

        return result;
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
        List groups = getUrlDecodedCleanedTrimmedParameterValueList( paramMap, "groups");
        if ( groups != null ) {
            // groups specified as single comma-delimited string. used by everything else.
            context.setSpecifiedGroups(groups);
        }
        else {
            // groups specified by group of checkboxes with name "groups". used by bulk-edit.
            context.setSpecifiedGroups(HtmlFormUtil.retrieveListOfCheckedCheckboxValues(paramMap, "groups"));
        }
        context.setSpecifiedUsers(getUrlDecodedCleanedTrimmedParameterValueList( paramMap, "users"));
        context.setLoggedInUser(getRemoteUser().getName());
		context.setKey(getKey());
    	context.setAdminAction(getUrlDecodedCleanedTrimmedParameterValue( paramMap, "adminAction"));
        context.setUserSearch(getUrlDecodedCleanedTrimmedParameterValue( paramMap, "userSearch"));
        return context;
    }

    public AdvancedUserQuery createAdvancedUserQuery() {
        AdvancedUserQuery userQuery = new AdvancedUserQuery();
        Map paramMap = ServletActionContext.getRequest().getParameterMap();
        userQuery.setLookupType(getUrlDecodedCleanedTrimmedParameterValue( paramMap, "lookupType"));
        userQuery.setPartialSearchTerm(getUrlDecodedCleanedTrimmedParameterValue( paramMap, "partialSearchTerm"));
        userQuery.setSubstringMatchType(getUrlDecodedCleanedTrimmedParameterValue( paramMap, "substringMatchType"));
        log.debug("Created advanced user query: " + userQuery.toString());
        return userQuery;
    }

    private void populateDataUnlessCached() {
        populateGroupsUnlessCached();
        populateUsersForSelectedGroupUnlessCached();
    }

    private void populateData() {
        findAndSetGroups();
        String selectedGroup = getSelectedGroup();
        if (!ConfigUtil.isNullOrEmpty(selectedGroup)) {
            findAndSetUsers(selectedGroup);
        }
    }

    private void populateGroupsUnlessCached() {
        if (getGroups()==null) {
            log.debug("getGroups() returned null so calling findAndSetGroups()");
            findAndSetGroups();
        }
        else {
            log.debug("returned cached groups.");
        }
    }

    private void populateUsersForSelectedGroupUnlessCached() {
        String selectedGroup = getSelectedGroup();
        if (!ConfigUtil.isNullOrEmpty(selectedGroup)) {
            if (getUsers()==null) {
                log.debug("getUsers() returned null so calling findAndSetUsers()");
                findAndSetUsers(selectedGroup);
            }
            else {
                log.debug("returned cached users. selectedGroup='" + selectedGroup + "'");
            }
        }
    }

    private void handlePaging(Map paramMap) {
        setPagerAction(getUrlDecodedCleanedTrimmedParameterValue( paramMap, "pagerAction"));
        String pagerAction = getPagerAction();
        if ("nextPageGroups".equals(pagerAction)) {
            PagerPaginationSupport groups = getGroups();
            if (hasNext(groups)) {
                next(groups);
            }
        }
        else if ("prevPageGroups".equals(pagerAction)) {
            PagerPaginationSupport groups = getGroups();
            if (hasPrev(groups)) {
                prev(groups);
            }
        }
        else if ("skipToGroup".equals(pagerAction)) {
            Integer recordNum = getRecordNum(paramMap);
            if (recordNum!=null) {
                // record num is one-based, not zero-based
                PagerPaginationSupportUtil.safelyMoveToOldStartIndex(new Integer(recordNum.intValue() - 1), getGroups());
            }
        }

        if ("nextPageUsers".equals(pagerAction)) {
            PagerPaginationSupport users = getUsers();
            if (hasNext(users)) {
                next(users);
            }
        }
        else if ("prevPageUsers".equals(pagerAction)) {
            PagerPaginationSupport users = getUsers();
            if (hasPrev(users)) {
                prev(users);
            }
        }
        else if ("skipToUser".equals(pagerAction)) {
            Integer recordNum = getRecordNum(paramMap);
            if (recordNum!=null) {
                // record num is one-based, not zero-based
                PagerPaginationSupportUtil.safelyMoveToOldStartIndex(new Integer(recordNum.intValue() - 1), getUsers());
            }
        }
    }

    private Integer getRecordNum(Map paramMap) {
        Integer result = null;
        String val = getUrlDecodedCleanedTrimmedParameterValue( paramMap, "recordNum");
        if (val!=null) {
            try {
                result = new Integer(val);
            }
            catch (NumberFormatException nfe) {
                log.warn("bad recordNum '" + val + "'", nfe);
            }
        }
        return result;
    }

    private void handleUserSearch() {
        if (getUserSearch()!=null) {
            AdvancedUserQuery advancedUserQuery = createAdvancedUserQuery();
            setAdvancedUserQuery(advancedUserQuery);
            setUserSearchFormFilled(advancedUserQuery.isValid());
            if (getUserSearchFormFilled()) {
                this.findUsersAdvanced();
            }
        }
    }

    private void handleRefreshData(Map paramMap) {
        if (getUrlDecodedCleanedTrimmedParameterValue(paramMap, "refresh")!=null) {
            refreshData();            
        }
    }

    private void refreshData() {
        Integer oldGroupsIndex = PagerPaginationSupportUtil.getStartIndexAsIntegerOrNull(getGroups());
        Integer oldUsersIndex = PagerPaginationSupportUtil.getStartIndexAsIntegerOrNull(getUsers());

        this.clearCache();
        this.populateData();

        // Note: is important that these are calling getGroups() and getUsers() again to get latest instances.
        PagerPaginationSupportUtil.safelyMoveToOldStartIndex(oldGroupsIndex, getGroups());
        PagerPaginationSupportUtil.safelyMoveToOldStartIndex(oldUsersIndex, getUsers());
    }

    // this gets around bug in atlassian's API CSP-10371
    // just append redirect= to end of URL and will redirect to the url.
    private void handleRedirect() {
        HttpServletRequest req = ServletActionContext.getRequest();
        // note: wrapping with TreeMap so it will sort params by name, otherwise it makes inconsistent URL which looks
        //       hackish.
        Map paramMap = new TreeMap(ServletActionContext.getRequest().getParameterMap());
        String[] redirect = (String[])paramMap.get(REDIRECT_PARAMNAME);
        if (redirect!=null) {
            HttpServletResponse resp = ServletActionContext.getResponse();

            // Redirect HTTP POST as well as HTTP GET. Remove REDIRECT_PARAMNAME from params.
            StringBuffer params = new StringBuffer();
            params.append("?");
            Iterator iter = paramMap.keySet().iterator();
            String paramConcat = "";
            while (iter.hasNext()) {
                String param = (String)iter.next();
                if (!REDIRECT_PARAMNAME.equals(param)) {
                    params.append(paramConcat);
                    params.append(bestAttemptUTF8Encode(param));
                    params.append("=");
                    String[] value = (String[])paramMap.get(param);
                    String valueConcat = "";
                    if (value!=null) {
                        params.append(valueConcat);
                        for (int i=0;i<value.length;i++) {
                            params.append(bestAttemptUTF8Encode(value[i]));
                        }                        
                        valueConcat = ",";
                    }
                    paramConcat = "&";
                }
            }

            String url = req.getRequestURI();
            int indexOfQuery = url.indexOf('?');
            if (indexOfQuery != -1) {
                // chop off query
                url = url.substring(0, indexOfQuery);
            }
            // readd query with any post params
            url = url + params.toString();

            log.debug("Workaround for bug CSP-10371. Redirecting to: " + url);
            try {
                resp.sendRedirect( url );
            }
            catch (IOException e) {
                log.error(e);
            }
        }
    }

    private String bestAttemptUTF8Encode(String s) {
        String result = s;
        try {
            result = URLEncoder.encode(s,"UTF-8");
        }
        catch (Throwable t) {
            log.error("Failed to URLEncode '" + s + "'", t);
        }
        return result;
    }

    private String[] bestAttemptUTF8Encode(String[] s) {
        String[] result = new String[s.length];
        for (int i=0;i<s.length;i++) {
            result[i] = bestAttemptUTF8Encode(s[i]);
        }
        return result;
    }

    public String execute() throws Exception
    {
		log.debug("CustomPermissionManagerAction.execute() called");

        log.debug("Starting execute() users");
        debug(getUsers());

        populateDataUnlessCached();

        CustomPermissionManagerActionContext context = createContext();

        //TODO: maybe this should be another adminAction.
        setUserSearch(context.getUserSearch());
        handleUserSearch();

        // only relevant for page itself, so not putting into context
        Map paramMap = ServletActionContext.getRequest().getParameterMap();

        setBulkEdit(getUrlDecodedCleanedTrimmedParameterValue(paramMap, "bulkEdit"));

        String selectedGroup = getUrlDecodedCleanedTrimmedParameterValue(paramMap, "selectedGroup");
        setSelectedGroup(selectedGroup);                

        // handle refresh
        handleRefreshData(paramMap);

        // handle paging
        handlePaging(paramMap);

        //TODO: remove this section before release!
        // START TEST SECTION
        if (getUrlDecodedCleanedTrimmedParameterValue(paramMap, "createTestUsersAndGroups")!= null) {
            // go nuts
            createTestUsersAndGroups();
        }
        // END TEST SECTION

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

        String result = this.manage(context);

        log.debug("Ending execute() users");
        debug(getUsers());

        if (result==SUCCESS && paramMap.get("adminAction") != null) {
            // refresh to avoid atlassian bug CSP-10371
            handleRedirect();
        }

        return result;
    }

    private void createTestUsersAndGroups() {
        try
        {
            SpaceDao spaceDao = (SpaceDao)ContainerManager.getComponent("spaceDao");
            Space space = spaceDao.getSpace(getKey());
            UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
            String prefix = GroupNameUtil.replaceSpaceKey(getCustomPermissionConfiguration().getNewGroupNameCreationPrefixPattern(), getKey());
            log.debug("group name prefix will be " + prefix);

            String suffix = GroupNameUtil.replaceSpaceKey(getCustomPermissionConfiguration().getNewGroupNameCreationSuffixPattern(), getKey());
            log.debug("group name suffix will be " + suffix);

            int numGroups = 40;
            int maxUsersPerGroup = 1000;
            int useridcount = 1;
            for (int i=1; i<=numGroups; i++) {
                String groupname = prefix + "tstgroup" + i + suffix;
                if (userAccessor.getGroup(groupname)==null) {
                    log.debug("Creating test group '" + groupname + "'");
                    userAccessor.createGroup(groupname);
                }
                log.debug("Adding permission '" + SpacePermission.VIEWSPACE_PERMISSION + "' to test group '" + groupname + "'");
                SpacePermission perm = new SpacePermission(SpacePermission.VIEWSPACE_PERMISSION, space, groupname);
                space.addPermission(perm);

                for (int j=1; j<=(maxUsersPerGroup - (maxUsersPerGroup/i) + 1); j++) {
                    String username = "tstuser" + useridcount;
                    if (userAccessor.getUser(username)==null) {
                        log.debug("Creating test user '" + username + "'");
                        userAccessor.createUser(username);
                    }

                    User user = userAccessor.getUser(username);
                    user.setEmail( username + "@duke.edu");
                    user.setFullName( "Test User " + useridcount );

                    log.debug("Adding test user '" + username + "' to group 'confluence-users'");
                    userAccessor.addMembership("confluence-users", username);
                    log.debug("Adding test user '" + username + "' to test group '" + groupname + "'");
                    userAccessor.addMembership(groupname, username);
                    useridcount++;
                }
            }
        }
        catch(Throwable t)
        {
            log.warn("Failed creating test groups/users", t);
        }

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
		log.debug("manage() called");

        List resultList = new ArrayList();
        String opMessage = null;
		String adminAction = context.getAdminAction();

        try
        {
            GroupManagementService groupManagementService = getGroupManagementService();
            UserManagementService userManagementService = getUserManagementService();
            ServiceContext serviceContext = createServiceContext();

            log.debug("adminAction=" + adminAction);

            if(adminAction != null && !"".equals(adminAction))
            {

                if(adminAction.equals(ACTION_ADD_USERS_TO_GROUPS) || adminAction.equals(ACTION_REMOVE_USERS_FROM_GROUPS)) {

                    //validate
                    if (ListUtil.isNullOrEmpty(context.getSpecifiedUsers())) {
                        log.warn("Failed action " + adminAction + ". users were null");
                        resultList.add("users cannot be null");
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    if (ListUtil.isNullOrEmpty(context.getSpecifiedGroups())) {
                        log.warn("Failed action " + adminAction + ". groups were null");
                        resultList.add("groups cannot be null");
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    int usersSize = context.getSpecifiedGroups().size();
                    int maxUserIDsLimit = new Integer(this.getCustomPermissionConfiguration().getMaxUserIDsLimit()).intValue();
                    if (usersSize > maxUserIDsLimit) {
                        log.warn("Failed action " + adminAction + ". users.size() = " + usersSize + " > configured maxUserIDsLimit " + maxUserIDsLimit);
                        resultList.add("cannot act on more than " + maxUserIDsLimit + " users. you tried to act on " + usersSize + " users, which is more than the amount allowed. the maximum value is set in the plugin's configuration");
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    int groupsSize = context.getSpecifiedGroups().size();
                    int maxGroupIDsLimit = new Integer(this.getCustomPermissionConfiguration().getMaxGroupIDsLimit()).intValue();
                    if (groupsSize > maxGroupIDsLimit) {
                        log.warn("Failed action " + adminAction + ". groups.size() = " + groupsSize + " > configured maxGroupIDsLimit " + maxGroupIDsLimit);
                        resultList.add("cannot act on more than " + maxGroupIDsLimit + " groups. you tried to act on " + groupsSize + " groups, which is more than the amount allowed. the maximum value is set in the plugin's configuration");
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    // get the old instance's paging index
                    Integer oldUsersIndex = PagerPaginationSupportUtil.getStartIndexAsIntegerOrNull(getUsers());
                    try {
                        if(adminAction.equals(ACTION_ADD_USERS_TO_GROUPS))
                        {
                            userManagementService.addUsersByUsernameToGroups(context.getSpecifiedUsers(), context.getSpecifiedGroups(), serviceContext);
                            opMessage = "<font color=\"green\">User(s) " + StringUtil.convertCollectionToCommaDelimitedString(context.getSpecifiedUsers()) + " added to group(s) " + StringUtil.convertCollectionToCommaDelimitedString(context.getSpecifiedGroups()) + " successfully!</font>";
                        }
                        else if(adminAction.equals(ACTION_REMOVE_USERS_FROM_GROUPS))
                        {
                            userManagementService.removeUsersByUsernameFromGroups(context.getSpecifiedUsers(), context.getSpecifiedGroups(), serviceContext);
                            opMessage = "<font color=\"green\">User(s) " + StringUtil.convertCollectionToCommaDelimitedString(context.getSpecifiedUsers()) + " removed from group(s) " + StringUtil.convertCollectionToCommaDelimitedString(context.getSpecifiedGroups()) + " successfully!</font>";
                        }
                    }
                    finally {

                        // clear user cache and repopulate
                        this.clearUserCache(context.getKey(), context.getSpecifiedGroups());
                        this.populateData();

                        // NOTE: intentionally calling getUsers() again because it is a new instance!
                        PagerPaginationSupportUtil.safelyMoveToOldStartIndex(oldUsersIndex, getUsers());
                    }
                }
                else if(adminAction.equals(ACTION_ADD_GROUPS) || adminAction.equals(ACTION_REMOVE_GROUPS)) {

                    //validate
                    if (ListUtil.isNullOrEmpty(context.getSpecifiedGroups())) {
                        log.warn("Failed action " + adminAction + ". users were null");
                        resultList.add("groups cannot be null");
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    int groupsSize = context.getSpecifiedGroups().size();
                    int maxGroupIDsLimit = new Integer(this.getCustomPermissionConfiguration().getMaxGroupIDsLimit()).intValue();
                    if (groupsSize > maxGroupIDsLimit) {
                        log.warn("Failed action " + adminAction + ". groups.size() = " + groupsSize + " > configured maxGroupIDsLimit " + maxGroupIDsLimit);
                        resultList.add("cannot act on more than " + maxGroupIDsLimit + " groups. you tried to act on " + groupsSize + " groups, which is more than the amount allowed. the maximum value is set in the plugin's configuration");
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    // get the old instance's paging index
                    Integer oldGroupsIndex = PagerPaginationSupportUtil.getStartIndexAsIntegerOrNull(getGroups());
                    Integer oldUsersIndex = PagerPaginationSupportUtil.getStartIndexAsIntegerOrNull(getUsers());

                    boolean usersAdded = false;
                    try {
                        if(adminAction.equals(ACTION_ADD_GROUPS))
                        {
                            String prefix = GroupNameUtil.replaceSpaceKey(getCustomPermissionConfiguration().getNewGroupNameCreationPrefixPattern(), space.getKey());
                            log.debug("group name prefix will be " + prefix);
                            String suffix = GroupNameUtil.replaceSpaceKey(getCustomPermissionConfiguration().getNewGroupNameCreationSuffixPattern(), space.getKey());
                            log.debug("group name suffix will be " + suffix);
                            List fixedGroupNames = new ArrayList();
                            List oldGroupNames = context.getSpecifiedGroups();
                            for (int i=0; i<oldGroupNames.size(); i++) {
                                String oldGroupName = (String)oldGroupNames.get(i);
                                String newGroupName = prefix + oldGroupName + suffix;
                                fixedGroupNames.add(newGroupName);
                            }

                            groupManagementService.addGroups(fixedGroupNames, serviceContext);
                            opMessage = "<font color=\"green\">Group(s) " + StringUtil.convertCollectionToCommaDelimitedString(fixedGroupNames) + " added successfully!</font>";

                            List specifiedUsers = context.getSpecifiedUsers();
                            if (specifiedUsers!=null && specifiedUsers.size()>0) {

                                int usersSize = context.getSpecifiedGroups().size();
                                int maxUserIDsLimit = new Integer(this.getCustomPermissionConfiguration().getMaxUserIDsLimit()).intValue();
                                if (usersSize > maxUserIDsLimit) {
                                    log.warn("Failed action " + adminAction + ". users.size() = " + usersSize + " > configured maxUserIDsLimit " + maxUserIDsLimit);
                                    resultList.add("cannot act on more than " + maxUserIDsLimit + " users. you tried to act on " + usersSize + " users, which is more than the amount allowed. the maximum value is set in the plugin's configuration");
                                    setActionErrors(resultList);
                                    return ERROR;
                                }

                                // get the old instance's paging index
                                usersAdded = true;
                                userManagementService.addUsersByUsernameToGroups(specifiedUsers, fixedGroupNames, serviceContext);
                            }
                        }
                        else if(adminAction.equals(ACTION_REMOVE_GROUPS))
                        {
                            List specifiedGroups = context.getSpecifiedGroups();

                            groupManagementService.removeGroups(specifiedGroups, serviceContext);
                            opMessage = "<font color=\"green\">Group(s) " + StringUtil.convertCollectionToCommaDelimitedString(context.getSpecifiedGroups()) + " removed successfully!</font>";
                            // groups no longer exist. remove cached group memberships if any.
                            this.clearUserCache(context.getKey(), specifiedGroups);
                        }
                    }
                    finally {

                        // clear group cache and repopulate
                        this.clearGroupCache(context.getKey());
                        if (usersAdded) {
                            this.clearUserCache(context.getKey(), context.getSpecifiedGroups());
                        }

                        this.populateData();

                        // NOTE: intentionally calling getGroups() because it is a new instance
                        PagerPaginationSupportUtil.safelyMoveToOldStartIndex(oldGroupsIndex, getGroups());

                        if (usersAdded) {
                            // NOTE: intentionally calling getUsers() because it is a new instance
                            PagerPaginationSupportUtil.safelyMoveToOldStartIndex(oldUsersIndex, getUsers());
                        }
                    }
                }
                else {
                    log.warn("Unrecognized adminAction='" + adminAction + "'");
                }
            }

            // note: is normal at times not to have an action (selecting group for example)
        }
        catch (IdListException e) {
            String msg = e.getMessage() + ": " + e.getIdsAsCommaDelimitedString();
            log.error(msg, e);
            resultList.add( msg );
            setActionErrors(resultList);
            return ERROR;
        }
        catch(Throwable t)
        {
            log.error("Failed action", t);
            resultList.add("" + t.getMessage());
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

    //public String getUserManagerLocation() {
    //    return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_USER_MANAGER_LOCATION);
	//}

	//public String getIsLdapAuthUsed()
    //{
    //    return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_LDAP_AUTH_STATUS_KEY);
    //}

    //public String getJiraJNDILookupKey()
    //{
    //    return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_JIRA_JNDI_KEY);
    //}

	//public String getCompanyLDAPBaseDN() {
    //    return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_COMPANY_LDAP_BASE_DN_KEY);
	//}

	//public String getCompanyLDAPUrl() {
    //    return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_COMPANY_LDAP_URL_KEY);
	//}

	//Get the count which indicates total no. of userids that can be processed at a time.
	//public String getMaxUserIDsLimit() {
    //    return ((String) bandanaManager.getValue(new ConfluenceBandanaContext(), CustomPermissionConfigConstants.DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT));
	//}

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

    public ConfigValidationResponse getConfigValidationResponse() {
        log.debug("getConfigValidationResponse() called");

        ConfigValidationResponse resp = this.getCustomPermissionConfiguration().validate();
        log.debug("getConfigValidationResponse.isValid = " + resp.isValid());
        return resp;
    }

    public boolean getIsGroupActionsPermitted() {
        log.debug("getIsGroupActionsPermitted() called");

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

    public void findAndSetGroups() {

        log.debug("findAndSetGroups() called");

        Pager pager = null;
        try {
            ServiceContext serviceContext = createServiceContext();
            pager = this.getGroupManagementService().findGroups(serviceContext);

            if (log.isDebugEnabled()) {
                debug(pager);
            }
        } catch (Throwable t) {
            log.error("Failed attempting to find groups", t);
        }

        setGroups(createPagerPaginationSupport(pager));
    }

    public PagerPaginationSupport createPagerPaginationSupport(Pager pager) {
        return createPagerPaginationSupport(pager, getRowsPerPage());
    }

    public PagerPaginationSupport createPagerPaginationSupport(Pager pager, int rowsPerPage) {
        if (pager==null) {
            return null;
        }

        PagerPaginationSupport pps = new PagerPaginationSupport(rowsPerPage);
        pps.setItems(pager);
        pps.setStartIndex(0);
        return pps;
    }

    private void debug(PagerPaginationSupport pps) {
        if (pps!=null) {
            log.debug( "pps hashCode=" + pps.hashCode());
            log.debug( "pps.getTotal()=" + pps.getTotal() );
            //log.debug( "pps.getItems() (following lines)" );
            debug(pps.getItems());
        }
    }

    private void debug(Pager pager) {
        if (pager!=null) {
            log.debug( "pager hashCode=" + pager.hashCode());
            log.debug( "PagerUtils.count(pager)=" + PagerUtils.count(pager));
            //log.debug( "pager.getIndex()=" + pager.getIndex());
            //log.debug( "pager.getIndexOfFirstItemInCurrentPage()=" + pager.getIndexOfFirstItemInCurrentPage());
            //log.debug( "pager.isEmpty()=" + pager.isEmpty());
            //log.debug( "pager.onLastPage()=" + pager.onLastPage());
        }
        else {
            log.debug("pager was null");
        }
    }

    public void findAndSetUsers(String groupName) {
        log.debug("findAndSetUsers() called. groupName='" + groupName + "'");

        if ( groupName != null ) {
            Pager pager = null;
            try {
                ServiceContext serviceContext = createServiceContext();
                pager = this.getUserManagementService().findUsersForGroup(groupName, serviceContext);
            } catch (Throwable t) {
                log.error("Failed finding users", t);
            }

            setUsers(createPagerPaginationSupport(pager));
        }
        else {
            log.debug("findAndSetUsers shouldn't be called with null groupName. programming error");
        }
    }

    public String getNewGroupPrefix() {
        return GroupNameUtil.replaceSpaceKey(getCustomPermissionConfiguration().getNewGroupNameCreationPrefixPattern(), space.getKey());
    }

    public String getNewGroupSuffix() {
        return GroupNameUtil.replaceSpaceKey(getCustomPermissionConfiguration().getNewGroupNameCreationSuffixPattern(), space.getKey());
    }

    public String findUsersWhoseNameStartsWith(String partialName, int numResults) {

        log.debug("findUsersWhoseNameStartsWith(" + partialName + "," + numResults + ") called");

        StringBuffer sb = new StringBuffer();
        try {
            ServiceContext serviceContext = createServiceContext();
            Pager pager = this.getUserManagementService().findUsersWhoseNameStartsWith(partialName, serviceContext);
            //this assumes you want the same number back as what would be displayed in a page, which could be really wrong...
            //may
            PagerPaginationSupport pps = this.createPagerPaginationSupport(pager, numResults);
            List users = pps.getPage();
            boolean gotAtLeastOne = false;
            if (users!=null) {
                for (int i=0; i<users.size(); i++) {
                    User user = (User)users.get(i);
                    String username = user.getName();
                    if (gotAtLeastOne) {
                        sb.append(", ");
                    }

                    sb.append( "\"" + username + "\"");
                    gotAtLeastOne = true;
                }
            }

        } catch (Throwable t) {
            log.error("Failed finding users that start with " + partialName, t);
        }

        return sb.toString();
    }
    /*
    <option value="$action.usernameLookupType" selected="selected">Username</option>
                        <option value="$action.fullNameLookupType">Full name</option>
                        <option value="$action.emailLookupType">Email</option>
    */

    //TODO: is there a better way to access this?
    public String getUsernameLookupType() {
        return AdvancedUserQueryLookupType.USERNAME;
    }

    //TODO: is there a better way to access this?
    public String getFullNameLookupType() {
        return AdvancedUserQueryLookupType.USER_FULL_NAME;
    }

    //TODO: is there a better way to access this?
    public String getEmailLookupType() {
        return AdvancedUserQueryLookupType.USER_EMAIL;
    }

    //TODO: is there a better way to access this?
    public String getSubstringContains() {
        return AdvancedUserQuerySubstringMatchType.SUBSTRING_CONTAINS;
    }

    //TODO: is there a better way to access this?
    public String getSubstringEndsWith() {
        return AdvancedUserQuerySubstringMatchType.SUBSTRING_ENDS_WITH;
    }

    //TODO: is there a better way to access this?
    public String getSubstringStartsWith() {
        return AdvancedUserQuerySubstringMatchType.SUBSTRING_STARTS_WITH;
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

    public String getBulkEdit() {
        return bulkEdit;
    }

    public void setBulkEdit(String bulkEdit) {
        this.bulkEdit = bulkEdit;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    private void addFieldErrorIfMessageNotNull(String nameOfField, String error) {
        if (error!=null) {
            log.warn("setting fieldError " + error + " on " + nameOfField);
            addFieldError(nameOfField, error);
        }
    }

    public void findUsersAdvanced() {

        log.debug("findUsersAdvanced() called");

        Pager pager = null;
        try {
            ServiceContext serviceContext = createServiceContext();
            AdvancedUserQuery query = getAdvancedUserQuery();
            AdvancedUserQueryResults results = this.getUserManagementService().findUsers(query, serviceContext);

            addFieldErrorIfMessageNotNull("advancedSearch",results.getMessage());

            pager = results.getUsers();
        } catch (Throwable t) {
            log.warn("Failed creating test groups/users", t);
        }

        setUsers(createPagerPaginationSupport(pager));
    }

    public boolean isMemberOfSelectedGroup(String userName) {

        log.debug("isMemberOfSelectedGroup(" + userName + ") called");

        boolean result = false;

        try {
            result = this.getUserManagementService().isMemberOf(userName, this.getSelectedGroup());
        } catch (Throwable t) {
            log.warn("Failed checking to see if user was member of group", t);
        }

        return result;
    }

    public String getSpacePattern() {
        return GroupNameUtil.replaceSpaceKey(this.getCustomPermissionConfiguration().getUserGroupsMatchingPattern(), getSpace().getKey());
    }

    public String getConfluenceRoot() {
        return ConfluenceUtil.getConfluenceUrl(getSettingsManager());
    }    

    public String getActionName(String fullClassName)
    {
    	return "Custom Space Usergroups Manager";
    }

    public PagerPaginationSupport getGroups() {
        String spaceKey = getKey();
        return getGroupsPps(spaceKey);
    }

    public void setGroups(PagerPaginationSupport groups) {
        String spaceKey = getKey();
        setGroupsPps(spaceKey, groups);
    }

    public PagerPaginationSupport getUsers() {
        String spaceKey = getKey();
        String selectedGroup = getSelectedGroup();
        return getUsersPps(spaceKey, selectedGroup);
    }

    public void setUsers(PagerPaginationSupport users) {
        String spaceKey = getKey();
        String selectedGroup = getSelectedGroup();
        setUsersPps(spaceKey, selectedGroup, users);
    }

    public String getPagerAction() {
        return pagerAction;
    }

    public void setPagerAction(String pagerAction) {
        this.pagerAction = pagerAction;
    }

    public boolean hasNext(PagerPaginationSupport pps) {
        return PagerPaginationSupportUtil.hasNext(pps);
    }

    public void next( PagerPaginationSupport pps ) {
        PagerPaginationSupportUtil.next(pps);
    }

    public boolean hasPrev(PagerPaginationSupport pps) {
        return PagerPaginationSupportUtil.hasPrev(pps);
    }

    public void prev( PagerPaginationSupport pps ) {
        PagerPaginationSupportUtil.prev(pps);
    }

    public String firstRecordNum( PagerPaginationSupport pps ) {
        String result = null;
        if (pps!=null) {
            result = "" + (pps.getStartIndex() + 1);
        }
        return result;
    }

    public String lastRecordNum( PagerPaginationSupport pps ) {
        String result = null;
        if (pps!=null) {
            result = "" + (PagerPaginationSupportUtil.getPageEndIndex(pps) + 1);
        }
        return result;
    }

    public List getAllGroups() {
        log.debug("getAllGroups() called");
        populateGroupsUnlessCached();
        List result = null;
        if (getGroups()!=null) {
            if (getGroups().getTotal() > 0) {
                result = PagerPaginationSupportUtil.toList(getGroups());
                GroupUtil.sortGroupsByGroupnameAscending(result);
                log.debug("returning " + result.size() + " groups");                
            }
            else {
                log.debug("getGroups().getTotal() was 0");
            }
        }
        else {
            log.debug("no groups found");
        }

        return result;
    }

    // TODO: need to refactor the meat of this and put it into CustomPermissionConfiguration class
    public boolean getIsUserSearchEnabled() {
        log.debug("getIsUserSearchEnabled() called");

        boolean isUserSearchEnabled = false;
        String userSearchEnabled = getCustomPermissionConfiguration().getUserSearchEnabled();
        if (ConfigUtil.isNotNullAndIsYesOrNo(userSearchEnabled)) {
            if ("YES".equals(userSearchEnabled)) {
                isUserSearchEnabled = true;
            }
        }
        log.debug("isUserSearchEnabled = " + isUserSearchEnabled);
        return isUserSearchEnabled;
    }

    public void logPps(PagerPaginationSupport pps) {
        if (pps!=null) {
            log.debug("VELOCITY SHOWING USERS " + pps.hashCode());
            debug(pps);
        }
        else {
            log.debug("VELOCITY SHOWING NULL USERS");
        }
    }

    public List getRanges(PagerPaginationSupport pps, int roughNumberOfRanges) {
        return PagerPaginationSupportUtil.getRanges(pps, roughNumberOfRanges);
    }
  }