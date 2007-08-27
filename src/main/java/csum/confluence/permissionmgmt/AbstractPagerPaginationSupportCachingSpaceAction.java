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

package csum.confluence.permissionmgmt;

import bucket.core.actions.PagerPaginationSupport;
import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAdministrative;
import com.opensymphony.xwork.ActionContext;
import csum.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Gary S. Weaver
 */
public abstract class AbstractPagerPaginationSupportCachingSpaceAction extends AbstractSpaceAction implements SpaceAdministrative {

    private static final Log staticlog = LogFactory.getLog(AbstractPagerPaginationSupportCachingSpaceAction.class);

    private static final String PLUGIN_SESSION_KEY_PREFIX = "SUSR";
    private static final String DELIMITER = ":";
    private static final String GROUPS_SESSION_KEY_SUFFIX = "groups";
    private static final String USERS_SESSION_KEY_SUFFIX = "users";
    private static final String SEARCH_RESULT_USERS_SESSION_KEY_SUFFIX = "searchresultusers";
    private static final String INDEX_SUFFIX = "-index";

    private static final String GROUPS_INDEX_SESSION_KEY_SUFFIX = GROUPS_SESSION_KEY_SUFFIX + INDEX_SUFFIX;
    private static final String USERS_INDEX_SESSION_KEY_SUFFIX = USERS_SESSION_KEY_SUFFIX + INDEX_SUFFIX;
    private static final String SEARCH_RESULT_USERS_INDEX_SESSION_KEY_SUFFIX = SEARCH_RESULT_USERS_SESSION_KEY_SUFFIX + INDEX_SUFFIX;
    private static final String ADVANCED_USER_QUERY_SESSION_KEY_SUFFIX = "advanceduserquery";

    private Object getSessionProperty(String key) {
        Map session = (Map) ActionContext.getContext().get("session");
        return session.get(key);
    }

    private void setSessionProperty(String key, Object value) {
        Map session = (Map) ActionContext.getContext().get("session");
        if ( value != null ) {
            session.put(key, value);
        }
        else {
            session.remove(key);
        }
    }

    private String id(Object o) {
        if (o!=null) {
            return "" + o.hashCode();
        }

        return "null";
    }

    // GROUP LIST AND GROUP LIST INDEX CACHING

    public PagerPaginationSupport getGroupsPps(String spaceKey) {
        PagerPaginationSupport result = null;
        try {
            result = (PagerPaginationSupport)getSessionProperty( getGroupsPpsKey(spaceKey) );
            log.debug("Got groups instance " + id(result) + " for spaceKey '" + spaceKey + "'");
        }
        catch (java.lang.ClassCastException e) {
            log.error("Invalid type stored in cache. Returning null", e);
        }
        return result;
    }

    public void setGroupsPps(String spaceKey, PagerPaginationSupport groupsPps) {
        setSessionProperty( getGroupsPpsKey(spaceKey), groupsPps);
        log.debug("Set groups instance " + id(groupsPps) + " for spaceKey '" + spaceKey + "'");
    }

    private String getGroupsPpsKey(String spaceKey) {
        return PLUGIN_SESSION_KEY_PREFIX + DELIMITER + spaceKey + DELIMITER + GROUPS_SESSION_KEY_SUFFIX;
    }

    public Integer getGroupsIndex(String spaceKey) {
        Integer result = null;
        try {
            result = (Integer)getSessionProperty( getGroupsIndexKey(spaceKey) );
            log.debug("Got groups index instance " + id(result) + " for spaceKey '" + spaceKey + "'");
        }
        catch (java.lang.ClassCastException e) {
            log.error("Invalid type stored in cache. Returning null", e);
        }
        return result;
    }

    public void setGroupsIndex(String spaceKey, Integer index) {
        setSessionProperty( getGroupsIndexKey(spaceKey), index);
        log.debug("Set groups index instance " + id(index) + " for spaceKey '" + spaceKey + "'");
    }

    private String getGroupsIndexKey(String spaceKey) {
        return PLUGIN_SESSION_KEY_PREFIX + DELIMITER + spaceKey + DELIMITER + GROUPS_INDEX_SESSION_KEY_SUFFIX;
    }

    // USERS LIST AND GROUP LIST INDEX CACHING

    public PagerPaginationSupport getUsersPps(String spaceKey, String groupName) {
        PagerPaginationSupport result = null;
        try {
            result = (PagerPaginationSupport)getSessionProperty( getUsersPpsKey(spaceKey, groupName) );
            log.debug("Got users instance " + id(result) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
        }
        catch (java.lang.ClassCastException e) {
            log.error("Invalid type stored in cache. Returning null", e);
        }
        return result;
    }

    public void setUsersPps(String spaceKey, String groupName, PagerPaginationSupport usersPps) {
        setSessionProperty( getUsersPpsKey(spaceKey, groupName), usersPps);
        log.debug("Set users instance " + id(usersPps) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
    }

    private String getUsersPpsKey(String spaceKey, String groupName) {
        return PLUGIN_SESSION_KEY_PREFIX + DELIMITER + spaceKey + DELIMITER + groupName + DELIMITER + USERS_SESSION_KEY_SUFFIX;
    }

    public Integer getUsersIndex(String spaceKey, String groupName) {
        Integer result = null;
        try {
            result = (Integer)getSessionProperty( getUsersIndexKey(spaceKey, groupName) );
            log.debug("Got users index instance " + id(result) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
        }
        catch (java.lang.ClassCastException e) {
            log.error("Invalid type stored in cache. Returning null", e);
        }
        return result;
    }

    public void setUsersIndex(String spaceKey, String groupName, Integer index) {
        setSessionProperty( getUsersIndexKey(spaceKey, groupName), index);
        log.debug("Set users index instance " + id(index) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
    }

    private String getUsersIndexKey(String spaceKey, String groupName) {
        return PLUGIN_SESSION_KEY_PREFIX + DELIMITER + spaceKey + DELIMITER + groupName + DELIMITER + USERS_INDEX_SESSION_KEY_SUFFIX;
    }

    // USER SEARCH-RELATED CACHING

    public PagerPaginationSupport getSearchResultUsersPps(String spaceKey, String groupName) {
        PagerPaginationSupport result = null;
        try {
            result = (PagerPaginationSupport)getSessionProperty( getSearchResultUsersPpsKey(spaceKey, groupName) );
            log.debug("Got search result users instance " + id(result) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
        }
        catch (java.lang.ClassCastException e) {
            log.error("Invalid type stored in cache. Returning null", e);
        }
        return result;
    }

    public void setSearchResultUsersPps(String spaceKey, String groupName, PagerPaginationSupport searchResultUsersPps) {
        setSessionProperty( getSearchResultUsersPpsKey(spaceKey, groupName), searchResultUsersPps);
        log.debug("Set search result users instance " + id(searchResultUsersPps) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
    }

    private String getSearchResultUsersPpsKey(String spaceKey, String groupName) {
        return PLUGIN_SESSION_KEY_PREFIX + DELIMITER + spaceKey + DELIMITER + groupName + DELIMITER + SEARCH_RESULT_USERS_SESSION_KEY_SUFFIX;
    }

    public Integer getSearchResultUsersIndex(String spaceKey, String groupName) {
        Integer result = null;
        try {
            result = (Integer)getSessionProperty( getSearchResultUsersIndexKey(spaceKey, groupName) );
            log.debug("Got search result users index instance " + id(result) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
        }
        catch (java.lang.ClassCastException e) {
            log.error("Invalid type stored in cache. Returning null", e);
        }
        return result;
    }

    public void setSearchResultUsersIndex(String spaceKey, String groupName, Integer index) {
        setSessionProperty( getSearchResultUsersIndexKey(spaceKey, groupName), index);
        log.debug("Set search result users index instance " + id(index) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
    }

    private String getSearchResultUsersIndexKey(String spaceKey, String groupName) {
        return PLUGIN_SESSION_KEY_PREFIX + DELIMITER + spaceKey + DELIMITER + groupName + DELIMITER + SEARCH_RESULT_USERS_INDEX_SESSION_KEY_SUFFIX;
    }

    public AdvancedUserQuery getAdvancedUserQuery(String spaceKey, String groupName) {
        AdvancedUserQuery result = null;
        try {
            result = (AdvancedUserQuery)getSessionProperty( getAdvancedUserQueryKey(spaceKey, groupName) );
            log.debug("Got advanced user query instance " + id(result) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
        }
        catch (java.lang.ClassCastException e) {
            log.error("Invalid type stored in cache. Returning null", e);
        }
        return result;
    }

    public void setAdvancedUserQuery(String spaceKey, String groupName, AdvancedUserQuery advancedUserQuery) {
        setSessionProperty( getAdvancedUserQueryKey(spaceKey, groupName), advancedUserQuery);
        log.debug("Set advanced user query instance " + id(advancedUserQuery) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
    }

    private String getAdvancedUserQueryKey(String spaceKey, String groupName) {
        return PLUGIN_SESSION_KEY_PREFIX + DELIMITER + spaceKey + DELIMITER + groupName + DELIMITER + ADVANCED_USER_QUERY_SESSION_KEY_SUFFIX;
    }


    // MISCELLANEOUS


    public void storeInPluginCache( String key, Object o ) {
        setSessionProperty(PLUGIN_SESSION_KEY_PREFIX + DELIMITER + key, o);
    }

    public Object getFromPluginCache( String key ) {
        Map session = (Map) ActionContext.getContext().get("session");
        return session.get(PLUGIN_SESSION_KEY_PREFIX + DELIMITER + key);
    }

    public void removeFromPluginCache( String key ) {
        Map session = (Map) ActionContext.getContext().get("session");
        session.remove(PLUGIN_SESSION_KEY_PREFIX + DELIMITER + key);
    }


    // CACHE-CLEARING METHODS


    // Note: this is a little dangerous as it makes an assumption that we want to cache every index for every pps
    private void cachePpsIndexIfSupported(String key) {
        Object o = getSessionProperty(key);
        if (o instanceof PagerPaginationSupport) {
            PagerPaginationSupport pps = (PagerPaginationSupport)o;
            setSessionProperty(key + INDEX_SUFFIX, new Integer(pps.getStartIndex()));
        }
    }        

    // called by config
    public static void clearCacheIncludingIndexes() {
        staticlog.debug("Clearing all cache (removing all session data with keys that start with '" + PLUGIN_SESSION_KEY_PREFIX + DELIMITER + "')");
        Map session = (Map) ActionContext.getContext().get("session");
        Iterator iter = session.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            // intentionally not removing index here
            if (key.startsWith(PLUGIN_SESSION_KEY_PREFIX + DELIMITER)) {
                session.remove(key);
            }
        }
    }

    // Note: intentionally not clearing index cache. That is used to find out where you were in result set.
    public void clearCache() {
        log.debug("Clearing all cache (removing all session data with keys that start with '" + PLUGIN_SESSION_KEY_PREFIX + "' and not ending with '" + INDEX_SUFFIX + "')");
        Map session = (Map) ActionContext.getContext().get("session");
        Iterator iter = session.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            // intentionally not removing index here
            if (key.startsWith(PLUGIN_SESSION_KEY_PREFIX + DELIMITER) && !key.endsWith(INDEX_SUFFIX)) {
                cachePpsIndexIfSupported(key);
                session.remove(key);
            }
        }
    }

    // Note: intentionally not clearing index cache. That is used to find out where you were in result set.
    public void clearGroupCache(String spaceKey) {
        Map session = (Map) ActionContext.getContext().get("session");
        String sessionKey = getGroupsPpsKey(spaceKey);
        log.debug("Clearing all groups cache for spacekey '" + spaceKey + "' (removing session data for '" + sessionKey + ")");
        cachePpsIndexIfSupported(sessionKey);
        session.remove(sessionKey);
    }

    // Note: intentionally not clearing index cache. That is used to find out where you were in result set.
    public void clearUserCache(String spaceKey, List groupNames) {        
        Map session = (Map) ActionContext.getContext().get("session");
        for (int i=0; i<groupNames.size(); i++) {
            String groupName = (String)groupNames.get(i);
            String sessionKey = getUsersPpsKey(spaceKey, groupName);
            log.debug("Clearing all users cache for spacekey '" + spaceKey + "' and groupName '" + groupName + "' (removing all session data for key '" + sessionKey + "')");
            cachePpsIndexIfSupported(sessionKey);
            session.remove(sessionKey);
        }
    }

    // Note: intentionally not clearing index cache. That is used to find out where you were in result set.
    public void clearSearchResultUserCache(String spaceKey, List groupNames) {
        Map session = (Map) ActionContext.getContext().get("session");
        for (int i=0; i<groupNames.size(); i++) {
            String groupName = (String)groupNames.get(i);
            String sessionKey = getSearchResultUsersPpsKey(spaceKey, groupName);
            log.debug("Clearing all search result users cache for spacekey '" + spaceKey + "' and groupName '" + groupName + "' (removing all session data for key '" + sessionKey + "')");
            cachePpsIndexIfSupported(sessionKey);
            session.remove(sessionKey);
        }
    }

    public String getCacheAsHtml() {
        StringBuffer sb = new StringBuffer();
        sb.append("<table><tr><td>Key</td><td>Data</td></tr>");
        Map session = (Map) ActionContext.getContext().get("session");
        Iterator iter = session.keySet().iterator();
        while (iter.hasNext()) {

            String key = (String)iter.next();

            // intentionally not removing index here
            if (key.startsWith(PLUGIN_SESSION_KEY_PREFIX + DELIMITER)) {
                sb.append("<tr>");
                sb.append("<td>" + key + "</td>");
                Object val = session.get(key);
                if (val instanceof PagerPaginationSupport) {
                    sb.append("<td>total size: " + ((PagerPaginationSupport)val).getTotal() + "</td>");
                }
                else if (val instanceof Integer) {
                    sb.append("<td>" + ((Integer)val).intValue() + "</td>");
                }
                else {
                    sb.append("<td>" + val + "</td>");
                }
                sb.append("</tr>");
            }

        }
        sb.append("</tr></table>");
        return sb.toString();
    }
}
