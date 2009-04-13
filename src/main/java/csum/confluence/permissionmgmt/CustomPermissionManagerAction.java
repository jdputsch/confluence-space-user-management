/**
 * Copyright (c) 2007-2009, Custom Space User Management Plugin Development Team
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

package csum.confluence.permissionmgmt;

import bucket.core.actions.PagerPaginationSupport;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.actions.SpaceAdministrative;
import com.atlassian.confluence.spaces.persistence.dao.SpaceDao;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.confluence.util.SpaceComparator;
import com.atlassian.user.User;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.webwork.ServletActionContext;
import csum.confluence.permissionmgmt.config.CsumConfigValidationResponse;
import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.service.CustomPermissionServiceManager;
import csum.confluence.permissionmgmt.service.GroupManagementService;
import csum.confluence.permissionmgmt.service.UserManagementService;
import csum.confluence.permissionmgmt.service.exception.ServiceException;
import csum.confluence.permissionmgmt.service.exception.UsersNotFoundException;
import csum.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import csum.confluence.permissionmgmt.service.vo.AdvancedUserQueryLookupType;
import csum.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;
import csum.confluence.permissionmgmt.service.vo.AdvancedUserQuerySubstringMatchType;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.util.ConfigUtil;
import csum.confluence.permissionmgmt.util.ListUtil;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.confluence.ConfluenceUtil;
import csum.confluence.permissionmgmt.util.logging.LogUtil;
import csum.confluence.permissionmgmt.util.cache.CacheUtil;
import csum.confluence.permissionmgmt.util.group.GroupNameUtil;
import csum.confluence.permissionmgmt.util.group.GroupUtil;
import csum.confluence.permissionmgmt.util.paging.PagerPaginationSupportUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The action that most of the work for the plugin centers around.
 *
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class CustomPermissionManagerAction extends AbstractPagerPaginationSupportCachingSpaceAction implements SpaceAdministrative
{
    private Log log = LogFactory.getLog(this.getClass());

    private SpaceDao spaceDao;
    private CustomPermissionServiceManager customPermissionServiceManager;
    private CustomPermissionConfiguration customPermissionConfiguration;
    private String selectedGroup;
    private String userSearch;
    private boolean userSearchFormFilled;
    private String bulkEdit;
    private SettingsManager settingsManager;
    private String pagerAction;
    private WebResourceManager webResourceManager;

    public static final String REDIRECT_PARAMNAME = "redirect";
    public static final String ADMIN_ACTION_PARAMNAME = "adminAction";
    public static final String USERS_PARAMNAME = "users";
    public static final String GROUPS_PARAMNAME = "groups";
    public static final String USER_SEARCH_PARAMNAME = "userSearch";
    public static final String ACTION_ADD_GROUPS = "addGroups";
    public static final String ACTION_REMOVE_GROUPS = "removeGroups";
    public static final String ACTION_ADD_USERS_TO_GROUPS = "addUsersToGroups";
    public static final String ACTION_REMOVE_USERS_FROM_GROUPS = "removeUsersFromGroups";
    public static final String ACTION_ADVANCED_FIND_USERS = "advancedFindUsers";
    public static final String REFRESH_BUG_SECOND_REQUEST_PARAMNAME = "conf9035";
    public static final String DEBUG = "debug";

    public CustomPermissionManagerAction()
	{
		log.debug("CustomPermissionManagerAction instance created");
    }

    public void setWebResourceManager(WebResourceManager webResourceManager)
    {
        this.webResourceManager = webResourceManager;
    }

    public boolean isConfluenceVersionEqualToOrAbove(String version) {
        return ConfluenceUtil.isConfluenceVersionEqualToOrAbove(version);
    }

    public String getActionDebug() {
        String result = null;

        if (this.getFromPluginCache(DEBUG)!=null) {

            try {
                StringBuffer sb = new StringBuffer();

                sb.append("isUserSearchEnabled=" + getIsUserSearchEnabled() + "<br/>");
                sb.append("selectedGroup=" + getSelectedGroup() + "<br/>");
                if (getGroups()!=null) {
                    sb.append("groups size=" + getGroups().getTotal() + "<br/>");
                    sb.append("groups current page first record=" + this.firstRecordNum(getGroups()) + "<br/>");
                    sb.append("groups current page last record=" + this.lastRecordNum(getGroups()) + "<br/>");
                }
                else {
                    sb.append("groups=null<br/>");
                }
                if (getUsers()!=null) {
                    sb.append("users size=" + getUsers().getTotal() + "<br/>");
                    sb.append("users current page first record=" + this.firstRecordNum(getUsers()) + "<br/>");
                    sb.append("users current page last record=" + this.lastRecordNum(getUsers()) + "<br/>");
                }
                else {
                    sb.append("users=null<br/>");
                }
                if (getSearchResultUsers()!=null) {
                    sb.append("searchResultUsers size=" + getSearchResultUsers().getTotal() + "<br/>");
                    sb.append("searchResultUsers current page first record=" + this.firstRecordNum(getSearchResultUsers()) + "<br/>");
                    sb.append("searchResultUsers current page last record=" + this.lastRecordNum(getSearchResultUsers()) + "<br/>");
                }
                else {
                    sb.append("searchResultUsers=null<br/>");
                }

                sb.append(getCacheAsHtml());

                result = sb.toString();
            }
            catch (Throwable t) {
                LogUtil.errorWithRemoteUserInfo(log, "Problem debugging action", t);
            }
        }

        return result;
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

    private List getUrlDecodedCleanedTrimmedParameterValues(Map paramMap, String param) {
        List result = null;
        String[] values = (String[])paramMap.get(param);
        if ( values != null ) {
            result = new ArrayList();
            for (int i=0;i<values.length;i++) {
                String value = cleanParamValue(param, values[i]);
                result.add(value);
            }
        }
        return result;
    }

    private String getUrlDecodedCleanedTrimmedParameterValue(Map paramMap, String param) {
        String value = getRawParameterValue(paramMap, param);
        return cleanParamValue(param, value);
    }

    private String cleanParamValue(String param, String value) {
        if (value!=null) {
            value = StringUtil.clean(value.trim());
            if (!StringUtil.isNullOrEmpty(value)) {
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    // NOTE: this should allow legal groupnames to pass so just log and ignore
                    log.debug("request parameter '" + param + "' had cleaned up value '" + value + "' which could not be URL decoded", e);
                }
                catch (IllegalArgumentException e) {
                    //TODO: write test for groupname sdfaa#$%#$%$%&
                    // NOTE: this should allow legal groupnames to pass so just log and ignore
                    log.debug("request parameter '" + param + "' had cleaned up value '" + value + "' which could not be URL decoded", e);
                }
            }
        }
        return value;
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

    private List getUrlDecodedCleanedTrimmedParameterValueListWithCheckboxSupport(Map paramMap, String param) {
        List result = new ArrayList();
        List decodedValues = getUrlDecodedCleanedTrimmedParameterValues(paramMap, param);
        if (decodedValues!=null) {
            for (int i=0; i<decodedValues.size(); i++) {
                String decodedValue = (String)decodedValues.get(i);
                result.addAll(StringUtil.getCleanedListFromDelimitedValueString(decodedValue));
            }
        }

        return result;
    }

    public ServiceContext createServiceContext() {
        ServiceContext context = new ServiceContext();
        // for i18n - to use same resource all over plugin
        context.setConfluenceActionSupport(this);
        context.setCustomPermissionConfigurable(getCustomPermissionConfiguration());
        context.setSpace(this.getSpace());
        return context;
    }

    public CustomPermissionManagerActionContext createContext() {
        CustomPermissionManagerActionContext context = new CustomPermissionManagerActionContext();
        Map paramMap = ServletActionContext.getRequest().getParameterMap();
        context.setSpecifiedGroups(getUrlDecodedCleanedTrimmedParameterValueListWithCheckboxSupport( paramMap, GROUPS_PARAMNAME));
        log.debug("groups=" + StringUtil.convertCollectionToCommaDelimitedString(context.getSpecifiedGroups()));
        context.setSpecifiedUsers(getUrlDecodedCleanedTrimmedParameterValueList( paramMap, USERS_PARAMNAME));
        log.debug("users=" + StringUtil.convertCollectionToCommaDelimitedString(context.getSpecifiedUsers()));
        context.setLoggedInUser(getRemoteUser().getName());
		log.debug("loggedInUser=" + context.getLoggedInUser());
        context.setKey(getKey());
        log.debug("key=" + context.getKey());
        context.setAdminAction(getUrlDecodedCleanedTrimmedParameterValue( paramMap, ADMIN_ACTION_PARAMNAME));
        log.debug("adminAction=" + context.getAdminAction());
        context.setUserSearch(getUrlDecodedCleanedTrimmedParameterValue( paramMap, USER_SEARCH_PARAMNAME));
        log.debug("userSearch=" + context.getUserSearch());
        context.setConfluenceActionSupport(this);
        return context;
    }

    public AdvancedUserQuery createAdvancedUserQuery() {
        AdvancedUserQuery userQuery = new AdvancedUserQuery();
        Map paramMap = ServletActionContext.getRequest().getParameterMap();
        userQuery.setLookupType(getUrlDecodedCleanedTrimmedParameterValue( paramMap, "lookupType"));
        log.debug("lookupType=" + userQuery.getLookupType());
        userQuery.setPartialSearchTerm(getUrlDecodedCleanedTrimmedParameterValue( paramMap, "partialSearchTerm"));
        log.debug("partialSearchTerm=" + userQuery.getPartialSearchTerm());
        userQuery.setSubstringMatchType(getUrlDecodedCleanedTrimmedParameterValue( paramMap, "substringMatchType"));
        log.debug("substringMatchType=" + userQuery.getSubstringMatchType());
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

        if ("nextPageSearchResultUsers".equals(pagerAction)) {
            PagerPaginationSupport users = getSearchResultUsers();
            if (hasNext(users)) {
                next(users);
            }
        }
        else if ("prevPageSearchResultUsers".equals(pagerAction)) {
            PagerPaginationSupport users = getSearchResultUsers();
            if (hasPrev(users)) {
                prev(users);
            }
        }
        else if ("skipToSearchResultUser".equals(pagerAction)) {
            Integer recordNum = getRecordNum(paramMap);
            if (recordNum!=null) {
                // record num is one-based, not zero-based
                PagerPaginationSupportUtil.safelyMoveToOldStartIndex(new Integer(recordNum.intValue() - 1), getSearchResultUsers());
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
                LogUtil.warnWithRemoteUserInfo(log, "bad recordNum '" + val + "'", nfe);
            }
        }
        return result;
    }

    private void handleUserSearch(CustomPermissionManagerActionContext context) {
        if (getUserSearch()!=null && getPagerAction()==null && ACTION_ADVANCED_FIND_USERS.equalsIgnoreCase(context.getAdminAction())) {
            log.debug("validating user search form. userSearch=" + getUserSearch() + " pagerAction=" + getPagerAction()+ " adminAction=" + context.getAdminAction());
            doUserSearch();
        }
        else {
            log.debug("not a user search. userSearch=" + getUserSearch() + " pagerAction=" + getPagerAction()+ " adminAction=" + context.getAdminAction());            
        }
    }

    private void doUserSearch() {
        AdvancedUserQuery advancedUserQuery = createAdvancedUserQuery();

        // get last good query so we can requery and show the users again. 
        if (!advancedUserQuery.isValid()) {
            advancedUserQuery = getAdvancedUserQuery();
        }

        setAdvancedUserQuery(advancedUserQuery);
        setUserSearchFormFilled(advancedUserQuery.isValid());
        if (getUserSearchFormFilled()) {
            log.debug("performing user search");
            this.findUsersAdvanced();
            this.setAdvancedUserQuery(getKey(), getSelectedGroup(), advancedUserQuery);
        }
    }

    private void handleRefreshData(Map paramMap) {
        if (getUrlDecodedCleanedTrimmedParameterValue(paramMap, "refresh")!=null) {
            refreshData();            
        }
    }

    private void refreshData() {
        clearCache();
        populateData();

        // Note: is important that these are calling getGroups() and getUsers() again to get latest instances.
        PagerPaginationSupportUtil.safelyMoveToOldStartIndex(getGroupsIndex(getKey()), getGroups());
        PagerPaginationSupportUtil.safelyMoveToOldStartIndex(getUsersIndex(getKey(), getSelectedGroup()), getUsers());
        PagerPaginationSupportUtil.safelyMoveToOldStartIndex(getSearchResultUsersIndex(getKey(), getSelectedGroup()), getSearchResultUsers());
    }

    private String bestAttemptUTF8Encode(String s) {
        String result = s;
        try {
            result = URLEncoder.encode(s,"UTF-8");
        }
        catch (Throwable t) {
            LogUtil.errorWithRemoteUserInfo(log, "Failed to URLEncode '" + s + "'", t);
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

    // this helps get around bug in atlassian's API CSP-10371/CONF-9035 to be fixed in confluence 2.6
    private void handleRefreshBugFirstRequest(String result) {

        if (getIsGroupMembershipRefreshFixEnabled()) {
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

                    // remove any params that could either do an additional redirect or additional action, except for groups
                    // because will need groups to know what cache to refresh on second request
                    if (!REDIRECT_PARAMNAME.equalsIgnoreCase(param) &&
                            !ADMIN_ACTION_PARAMNAME.equalsIgnoreCase(param) &&
                            !USERS_PARAMNAME.equalsIgnoreCase(param)
                            ) {
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

                // store result, etc in session to be removed after redirect
                storeInPluginCache("result", result);
                storeInPluginCache("actionmessages", this.getActionMessages());
                storeInPluginCache("actionerrors", this.getActionErrors());
                storeInPluginCache("fielderrors", this.getFieldErrors());

                // add a param that tells action to refresh only cache associated with currently selected group in next req
                params.append("&" + REFRESH_BUG_SECOND_REQUEST_PARAMNAME);

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
                    LogUtil.errorWithRemoteUserInfo(log, "Redirect failed!", e);
                }
            }
        }
    }

    // this helps get around bug in atlassian's API CSP-10371/CONF-9035 to be fixed in confluence 2.6
    private String handleRefreshBugSecondRequest(Map paramMap) {

        String result = null;

        if (this.getRawParameterValue(paramMap, REFRESH_BUG_SECOND_REQUEST_PARAMNAME) != null) {

            // RECOVER STATE

            result = (String)getFromPluginCache("result");
            setActionMessages((Collection)getFromPluginCache("actionmessages"));
            setActionErrors((Collection)getFromPluginCache("actionerrors"));
            setFieldErrors((Map)getFromPluginCache("fielderrors"));

            // CLEAR STATE

            removeFromPluginCache("result");
            removeFromPluginCache("actionmessages");
            removeFromPluginCache("actionerrors");
            removeFromPluginCache("fielderrors");

            // CLEAR SELECTED CACHE

            this.clearGroupCache(getKey());
            // this is why we have to leave groups param in the url in handleRedirect
            List groups = getUrlDecodedCleanedTrimmedParameterValueList(paramMap, GROUPS_PARAMNAME);
            if (groups!=null) { 
                this.clearSearchResultUserCache(getKey(), groups);
                doUserSearch();
                this.clearUserCache(getKey(), groups);
            }
        }

        return result;
    }


    private boolean isPersonalSpaceAdminAllowed() {
        boolean result = false;
        String personalSpaceAllowed = getCustomPermissionConfiguration().getPersonalSpaceAllowed();
        log.debug("PersonalSpaceAllowed=" + personalSpaceAllowed);
        if ("YES".equals(personalSpaceAllowed)) {
            result = true;
        }
        return result;
    }

    // MUST BE PUBLIC because is called from display.vm also
    public boolean isNotAllowed() {
        boolean isNotAllowed = false;

        // Is this a personal space, and is personal space administration not allowed?
        String spaceKey = getKey();
        if (spaceKey!=null && spaceKey.startsWith("~")) {
            if (!isPersonalSpaceAdminAllowed()) {
                log.info("Refused to allow " + getRemoteUser().getName() + " to administer users/groups in personal space " + spaceKey);
                isNotAllowed = true;
            }
        }
        else {
            log.debug("Space is not a personal space");
        }

        return isNotAllowed;
    }

    // this can't be done in the velocity template, and must be called via reflection because method doesn't exist in
    // earlier versions of Confluence < 2.8
    public void loadWebResourceIfConfluence2_8_OrHigher(WebResourceManager webResourceManager, String resource) {
        if (webResourceManager!=null) {
            Class importExportManagerClazz = webResourceManager.getClass();
            Method exportMethod;

            try {
                // Confluence >= 2.8(?) only has method (SUSR-75)
                // exportAs(ExportContext,ProgressMeter):String
                exportMethod = importExportManagerClazz.getMethod("requireResource", new Class[]{String.class});
                exportMethod.invoke(webResourceManager, new Object[]{resource});

                if (log.isDebugEnabled()) {
                    log.debug("loaded webresource '" + resource + "'");
                }

            } catch (NoSuchMethodException nsme) {
                // If this happens, we are most probably in Confluence < 2.8
                if (log.isDebugEnabled()) {
                    log.debug("Ignore the following exception for Confluence versions < 2.8 that do not support the " +
                              "method: webResourceManager.requireResource(String). Resource that wasn't loaded was '" +
                              resource + "'", nsme);
                }
            } catch (Exception e) {
                log.warn("Problem using reflection to load web resource: " + resource, e);
            }
        }
        else
        {
            log.warn("webResourceManager was not set on " + this.getClass() + " so didn't load webresource '" + resource + "'");
        }
    }

    public String execute() throws Exception
    {
		log.debug("CustomPermissionManagerAction.execute() called");
        log.debug("request uri: " + ServletActionContext.getRequest().getRequestURI());

        if (getSpace()==null) {
            log.warn("Space was null");
            List resultList = new ArrayList();
            resultList.add(getText("csum.display.alert.invalidspacekey"));
            setActionErrors(resultList);
            return ERROR;
        }

        if (getRemoteUser()==null) {
            log.warn("RemoteUser was null");
            List resultList = new ArrayList();
            resultList.add(getText("csum.display.alert.invaliduser"));
            setActionErrors(resultList);
            return ERROR;
        }

        loadWebResourceIfConfluence2_8_OrHigher(webResourceManager, CustomPermissionConstants.PLUGIN_KEY + ":" + CustomPermissionConstants.RESOURCE_BUNDLE_KEY);

        CacheUtil.setRemoteUser(getRemoteUser());

        // only relevant for page itself, so not putting into context
        Map paramMap = ServletActionContext.getRequest().getParameterMap();
        log.debug("paramMap: " + paramMap);


        if (paramMap != null && paramMap.get(DEBUG)!=null) {
            storeInPluginCache(DEBUG, paramMap.get(DEBUG));
        }

        if(isNotAllowed()) {
            LogUtil.warnWithRemoteUserInfo(log, "Action not allowed");
            List resultList = new ArrayList();
            resultList.add(getText("csum.display.alert.notallowed"));
            setActionErrors(resultList);
            return ERROR;
        }


        String result = handleRefreshBugSecondRequest(paramMap);

        String selectedGroup = getUrlDecodedCleanedTrimmedParameterValue(paramMap, "selectedGroup");
        log.debug("selectedGroup=" + selectedGroup);
        setSelectedGroup(selectedGroup);
        ServiceContext serviceContext = createServiceContext();

        // fix for bug: if group (due to permissions or config change) is no longer selectable, then set to null, clear cache
        if (selectedGroup != null) {
            boolean canManageSelectedGroup = getGroupManagementService().isAllowedToManageGroup(serviceContext,selectedGroup);
            log.debug("isAllowedToManage '" + selectedGroup + "' = " + canManageSelectedGroup);
            if (!canManageSelectedGroup) {
                setSelectedGroup(null);

                Collection actionErrors = getActionErrors();
                if (actionErrors==null) {
                    actionErrors = new ArrayList();
                }
                actionErrors.add(getText("csum.manager.error.notallowedtomanageselectedgroup") + ": " + selectedGroup);
                setActionErrors(actionErrors);

                // config or permissions have changed - blow away users/groups/search cache
                this.clearCache();
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Starting execute() users");
            debug(getUsers());
        }

        populateDataUnlessCached();

        CustomPermissionManagerActionContext context = createContext();

        //TODO: maybe this should be another adminAction, but if so will need to update handleUserSearch() to exclude it
        setUserSearch(context.getUserSearch());
        handleUserSearch(context);
        setBulkEdit(getUrlDecodedCleanedTrimmedParameterValue(paramMap, "bulkEdit"));

        //validate adminAction set if bulkedit submit
        if (paramMap.get("bulkEditSubmit")!=null) {
            if (context.getAdminAction()==null) {
                Collection actionErrors = getActionErrors();
                if (actionErrors==null) {
                    actionErrors = new ArrayList();
                }
                actionErrors.add(getText("csum.manager.error.bulkeditnoadminaction"));
                setActionErrors(actionErrors);
                return ERROR;
            }
        }

        // handle refresh
        handleRefreshData(paramMap);

        // handle paging
        handlePaging(paramMap);

        // START TEST SECTION
        //if (getUrlDecodedCleanedTrimmedParameterValue(paramMap, "createTestUsersAndGroups")!= null) {
            // go nuts
            //createTestUsersAndGroups();
        //}
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
        //String UserValidationMessage = validateUserGroupWikiSpaceAssociation(context.getSelectedGroups());
    	//if(UserValidationMessage != null)
    	//{
        //    log.debug("There are no groups this user can currently administer. message=" + UserValidationMessage);
        //    addFieldError("NotPermittedUserErrorMessage", UserValidationMessage);
    	//	return ERROR;
    	//}

        if ( result == null) {
            result = this.manage(context, serviceContext);
        }

        if (log.isDebugEnabled()) {
            log.debug("Ending execute() users");
            debug(getUsers());
        }

        // note: data needs to get updated even if was error, because could have been partially updated
        if (paramMap.get(ADMIN_ACTION_PARAMNAME) != null) {
            // refresh to avoid atlassian bug CSP-10371/CONF-9035
            handleRefreshBugFirstRequest(result);
        }

        return result;
    }

    /*
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
            LogUtil.warnWithRemoteUserInfo(log, "Failed creating test groups/users", t);
        }

    }
    */
    
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
        List spaceList = null;

        if (GeneralUtil.isSuperUser(user)) {
            spaceList = getAllSpaces();
        }
        else {
            spaceList = spaceDao.getPermittedSpacesForUser(user,permission);
        }

        if ( spaceList != null ) {
            //remove personal spaces if not allowed
            if (!isPersonalSpaceAdminAllowed()) {
                List newList = new ArrayList();
                for ( int i=0; i<spaceList.size(); i++ ) {
                    Space thisSpace = (Space)spaceList.get(i);
                    String key = thisSpace.getKey();
                    if (key != null && key.startsWith("~")) {
                        log.debug("Removing " + key + " from list of spaces that can be administered, since personal space administration not allowed" );
                    }
                    else {
                        newList.add(thisSpace);
                    }
                }
                spaceList = newList;
            }

            Collections.sort(spaceList, new SpaceComparator());

        }

        return spaceList;
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
        result.setConfluenceActionSupport(this);
        result.setCustomPermissionConfigurable(this.getCustomPermissionConfiguration());
        result.setSpace(getSpace());
        return result;
    }

    public String manage(CustomPermissionManagerActionContext context, ServiceContext serviceContext)
    {
		log.debug("manage() called");

        List resultList = new ArrayList();
        String opMessage = null;
		String adminAction = context.getAdminAction();

        try
        {
            GroupManagementService groupManagementService = getGroupManagementService();
            UserManagementService userManagementService = getUserManagementService();

            log.debug("adminAction=" + adminAction);

            if(adminAction != null && !"".equals(adminAction))
            {
                if (adminAction.equals(ACTION_ADVANCED_FIND_USERS)) {
                    //TODO: consider calling find from here
                    return SUCCESS;
                }
                else if(adminAction.equals(ACTION_ADD_USERS_TO_GROUPS) || adminAction.equals(ACTION_REMOVE_USERS_FROM_GROUPS)) {

                    //validate
                    if (ListUtil.isNullOrEmpty(context.getSpecifiedUsers())) {
                        LogUtil.warnWithRemoteUserInfo(log, "Failed action " + adminAction + ". users were null");
                        resultList.add(getText("csum.manager.error.usersnull"));
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    if (ListUtil.isNullOrEmpty(context.getSpecifiedGroups())) {
                        LogUtil.warnWithRemoteUserInfo(log, "Failed action " + adminAction + ". groups were null");
                        resultList.add(getText("csum.manager.error.groupsnull"));
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    int usersSize = context.getSpecifiedUsers().size();
                    int maxUserIDsLimit = new Integer(this.getCustomPermissionConfiguration().getMaxUserIDsLimit()).intValue();
                    if (usersSize > maxUserIDsLimit) {
                        String msg = getText("csum.manager.error.maxnumusersexceeded") + " " + maxUserIDsLimit + ".";
                        LogUtil.warnWithRemoteUserInfo(log, "Failed action " + adminAction + ". users.size() = " + usersSize + " > configured maxUserIDsLimit " + maxUserIDsLimit);
                        resultList.add(msg);
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    int groupsSize = context.getSpecifiedGroups().size();
                    int maxGroupIDsLimit = new Integer(this.getCustomPermissionConfiguration().getMaxGroupIDsLimit()).intValue();
                    if (groupsSize > maxGroupIDsLimit) {
                        String msg = getText("csum.manager.error.maxnumgroupsexceeded") + " " + maxGroupIDsLimit + ".";
                        LogUtil.warnWithRemoteUserInfo(log, "Failed action " + adminAction + ". groups.size() = " + groupsSize + " > configured maxGroupIDsLimit " + maxGroupIDsLimit);
                        resultList.add(msg);
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    // get the old instance's paging index
                    Integer oldUsersIndex = PagerPaginationSupportUtil.getStartIndexAsIntegerOrNull(getUsers());
                    //Integer oldSearchResultUsersIndex = PagerPaginationSupportUtil.getStartIndexAsIntegerOrNull(getSearchResultUsers());
                    try {
                        if(adminAction.equals(ACTION_ADD_USERS_TO_GROUPS))
                        {
                            userManagementService.addUsersByUsernameToGroups(context.getSpecifiedUsers(), context.getSpecifiedGroups(), serviceContext);
                            opMessage = getText("csum.manager.success.adduserstogroups");

                        }
                        else if(adminAction.equals(ACTION_REMOVE_USERS_FROM_GROUPS))
                        {
                            userManagementService.removeUsersByUsernameFromGroups(context.getSpecifiedUsers(), context.getSpecifiedGroups(), serviceContext);
                            opMessage = getText("csum.manager.success.removeusersfromgroups");
                        }
                    }
                    finally {

                        // clear user cache and repopulate
                        this.clearUserCache(context.getKey(), context.getSpecifiedGroups());
                        //this.clearSearchResultUserCache(context.getKey(), context.getSpecifiedGroups());
                        this.populateData();

                        // NOTE: intentionally calling getUsers() again because it is a new instance!
                        PagerPaginationSupportUtil.safelyMoveToOldStartIndex(oldUsersIndex, getUsers());
                        //PagerPaginationSupportUtil.safelyMoveToOldStartIndex(oldSearchResultUsersIndex, getSearchResultUsers());
                    }
                }
                else if(adminAction.equals(ACTION_ADD_GROUPS) || adminAction.equals(ACTION_REMOVE_GROUPS)) {

                    //validate
                    if (ListUtil.isNullOrEmpty(context.getSpecifiedGroups())) {
                        LogUtil.warnWithRemoteUserInfo(log, "Failed action " + adminAction + ". groups were null");
                        resultList.add(getText("csum.manager.error.groupsnull"));
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    int groupsSize = context.getSpecifiedGroups().size();
                    int maxGroupIDsLimit = new Integer(this.getCustomPermissionConfiguration().getMaxGroupIDsLimit()).intValue();
                    if (groupsSize > maxGroupIDsLimit) {
                        String msg = getText("csum.manager.error.maxnumgroupsexceeded") + " " + maxGroupIDsLimit + ".";
                        LogUtil.warnWithRemoteUserInfo(log, "Failed action " + adminAction + ". groups.size() = " + groupsSize + " > configured maxGroupIDsLimit " + maxGroupIDsLimit);
                        resultList.add(msg);
                        setActionErrors(resultList);
                        return ERROR;
                    }

                    // get the old instance's paging index
                    Integer oldGroupsIndex = PagerPaginationSupportUtil.getStartIndexAsIntegerOrNull(getGroups());
                    Integer oldUsersIndex = PagerPaginationSupportUtil.getStartIndexAsIntegerOrNull(getUsers());
                    //Integer oldSearchResultUsersIndex = PagerPaginationSupportUtil.getStartIndexAsIntegerOrNull(getSearchResultUsers());

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
                            opMessage = getText("csum.manager.success.addgroups");

                            List specifiedUsers = context.getSpecifiedUsers();
                            if (specifiedUsers!=null && specifiedUsers.size()>0) {

                                int usersSize = context.getSpecifiedUsers().size();
                                int maxUserIDsLimit = new Integer(this.getCustomPermissionConfiguration().getMaxUserIDsLimit()).intValue();
                                if (usersSize > maxUserIDsLimit) {
                                    String msg = getText("csum.manager.error.maxnumusersexceeded") + " " + maxUserIDsLimit + ".";
                                    LogUtil.warnWithRemoteUserInfo(log, "Failed action " + adminAction + ". users.size() = " + usersSize + " > configured maxUserIDsLimit " + maxUserIDsLimit);
                                    resultList.add(msg);
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
                            opMessage = getText("csum.manager.success.removegroups");

                            // groups no longer exist. remove cached group memberships if any.
                            this.clearUserCache(context.getKey(), specifiedGroups);
                        }
                    }
                    finally {

                        // clear group and user cache and repopulate
                        this.clearGroupCache(context.getKey());
                        this.clearUserCache(context.getKey(), context.getSpecifiedGroups());
                        //this.clearSearchResultUserCache(context.getKey(), context.getSpecifiedGroups());

                        this.populateData();

                        // NOTE: intentionally calling getGroups() because it is a new instance
                        PagerPaginationSupportUtil.safelyMoveToOldStartIndex(oldGroupsIndex, getGroups());
                        PagerPaginationSupportUtil.safelyMoveToOldStartIndex(oldUsersIndex, getUsers());
                        //PagerPaginationSupportUtil.safelyMoveToOldStartIndex(oldSearchResultUsersIndex, getSearchResultUsers());
                    }
                }
                else {
                    LogUtil.warnWithRemoteUserInfo(log, "Unrecognized adminAction='" + adminAction + "'");
                }
            }

            // note: is normal at times not to have an action (selecting group for example)
        }
        catch (UsersNotFoundException e) {
            LogUtil.debugWithRemoteUserInfo(log, "User(s) not found", e);
            resultList.add(e.getMessage());
            setActionErrors(resultList);
            return ERROR;
        }
        catch (ServiceException e) {
            LogUtil.errorWithRemoteUserInfo(log, "Service exception", e);
            resultList.add(e.getMessage());
            setActionErrors(resultList);
            return ERROR;
        }
        catch(Throwable t)
        {
            LogUtil.errorWithRemoteUserInfo(log, "Failed action", t);
            resultList.add(t.getMessage());
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

	public boolean isPluginDown() {
        return ConfigUtil.isNotNullAndIsYes(getCustomPermissionConfiguration().getPluginDown());
    }

    public String getPluginDownMessage() {
        String result = getCustomPermissionConfiguration().getDownTimeMessage();
        if ( result == null || result.trim().equals("")) {
            result = getText("csum.manager.downtimemessagedefault");
        }
        return result;
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

    public CsumConfigValidationResponse getConfigValidationResponse() {
        log.debug("getConfigValidationResponse() called");

        CsumConfigValidationResponse resp = this.getCustomPermissionConfiguration().validate(this);
        log.debug("getConfigValidationResponse.isValid = " + resp.isValid());
        return resp;
    }

    public boolean getIsGroupActionsPermitted() {
        return ConfigUtil.isNotNullAndIsYes(getCustomPermissionConfiguration().getGroupActionsPermitted());
    }

    public boolean getIsGroupMembershipRefreshFixEnabled() {
        return ConfigUtil.isNotNullAndIsYes(getCustomPermissionConfiguration().getGroupMembershipRefreshFixEnabled());
    }

    public GroupManagementService getGroupManagementService() throws ServiceException {
        return getCustomPermissionServiceManager().getGroupManagementService(this);
    }

    public UserManagementService getUserManagementService() throws ServiceException {
        return getCustomPermissionServiceManager().getUserManagementService(this);
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
            LogUtil.errorWithRemoteUserInfo(log, "Failed attempting to find groups", t);
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
        if (log.isDebugEnabled() && pps!=null) {
            log.debug( "pps hashCode=" + pps.hashCode());
            log.debug( "pps.getTotal()=" + pps.getTotal() );
            //log.debug( "pps.getItems() (following lines)" );
            debug(pps.getItems());
        }
    }

    private void debug(Pager pager) {
        if (log.isDebugEnabled()) {
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
    }

    public void findAndSetUsers(String groupName) {
        log.debug("findAndSetUsers() called. groupName='" + groupName + "'");

        if ( groupName != null ) {
            Pager pager = null;
            try {
                ServiceContext serviceContext = createServiceContext();
                pager = this.getUserManagementService().findUsersForGroup(groupName, serviceContext);
            } catch (Throwable t) {
                addActionError(getText("csum.manager.error.usersnotfound"));
                LogUtil.errorWithRemoteUserInfo(log, "Failed finding users for groupName '" + groupName + "'", t);
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
            LogUtil.errorWithRemoteUserInfo(log, "Failed finding users that start with " + partialName, t);
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
        String spaceKey = getKey();
        String selectedGroup = getSelectedGroup();
        AdvancedUserQuery advancedUserQuery = this.getAdvancedUserQuery(spaceKey, selectedGroup);
        if ( advancedUserQuery == null ) {
            advancedUserQuery = new AdvancedUserQuery();
            this.setAdvancedUserQuery(spaceKey, selectedGroup, advancedUserQuery);
        }
        return advancedUserQuery;
    }

    public void setAdvancedUserQuery(AdvancedUserQuery advancedUserQuery) {
        String spaceKey = getKey();
        String selectedGroup = getSelectedGroup();
        setAdvancedUserQuery(spaceKey, selectedGroup, advancedUserQuery);
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
            LogUtil.warnWithRemoteUserInfo(log, "setting fieldError " + error + " on " + nameOfField);
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
            LogUtil.warnWithRemoteUserInfo(log, "Failed advanced find users", t);
        }

        setSearchResultUsers(createPagerPaginationSupport(pager));
    }

    public boolean isMemberOfSelectedGroup(String userName) {

        log.debug("isMemberOfSelectedGroup(" + userName + ") called");

        boolean result = false;

        try {
            result = this.getUserManagementService().isMemberOf(userName, this.getSelectedGroup());
        } catch (Throwable t) {
            LogUtil.warnWithRemoteUserInfo(log, "Failed checking to see if user was member of group", t);
        }

        return result;
    }

    public String getSpacePattern() {
        Space space = getSpace();
        String result = null;
        if (space != null) {
            String lowercaseSpaceKey = getSpace().getKey().toLowerCase();
            result = GroupNameUtil.replaceSpaceKey(this.getCustomPermissionConfiguration().getNewGroupNameCreationPrefixPattern(), lowercaseSpaceKey) +
                    "*" +
                    GroupNameUtil.replaceSpaceKey(this.getCustomPermissionConfiguration().getNewGroupNameCreationSuffixPattern(), lowercaseSpaceKey);
        }

        return result;
    }

    public String getActionName(String fullClassName)
    {
    	return getText("csum.manager.action.name");
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

    public PagerPaginationSupport getSearchResultUsers() {
        String spaceKey = getKey();
        String selectedGroup = getSelectedGroup();
        return getSearchResultUsersPps(spaceKey, selectedGroup);
    }

    public void setSearchResultUsers(PagerPaginationSupport users) {
        String spaceKey = getKey();
        String selectedGroup = getSelectedGroup();
        setSearchResultUsersPps(spaceKey, selectedGroup, users);
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
        if (log.isDebugEnabled() && pps!=null) {
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