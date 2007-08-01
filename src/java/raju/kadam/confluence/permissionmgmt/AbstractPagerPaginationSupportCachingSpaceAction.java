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

package raju.kadam.confluence.permissionmgmt;

import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAdministrative;
import com.opensymphony.xwork.ActionContext;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import bucket.core.actions.PagerPaginationSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Gary S. Weaver
 */
public abstract class AbstractPagerPaginationSupportCachingSpaceAction extends AbstractSpaceAction implements SpaceAdministrative {

    private static final String PLUGIN_SESSION_KEY_PREFIX = "SUSR";
    private static final String GROUPS_SESSION_KEY_SUFFIX = "groups";
    private static final String USERS_SESSION_KEY_SUFFIX = "users";
    private static final String SEARCH_RESULT_USERS_SESSION_KEY_SUFFIX = "searchresultusers";

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

    private String id(PagerPaginationSupport pps) {
        if (pps!=null) {
            return "" + pps.hashCode();
        }

        return "null";
    }

    public PagerPaginationSupport getGroupsPps(String spaceKey) {
        PagerPaginationSupport result = (PagerPaginationSupport)getSessionProperty( getGroupsPpsKey(spaceKey) );
        log.debug("Got groups instance " + id(result) + " for spaceKey '" + spaceKey + "'");
        return result;
    }

    public void setGroupsPps(String spaceKey, PagerPaginationSupport groupsPps) {
        setSessionProperty( getGroupsPpsKey(spaceKey), groupsPps);
        log.debug("Set groups instance " + id(groupsPps) + " for spaceKey '" + spaceKey + "'");
    }

    private String getGroupsPpsKey(String spaceKey) {
        return PLUGIN_SESSION_KEY_PREFIX + ":" + spaceKey + ":" + GROUPS_SESSION_KEY_SUFFIX;
    }

    public PagerPaginationSupport getUsersPps(String spaceKey, String groupName) {
        PagerPaginationSupport result = (PagerPaginationSupport)getSessionProperty( getUsersPpsKey(spaceKey, groupName) );
        log.debug("Got users instance " + id(result) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
        return result;
    }

    public void setUsersPps(String spaceKey, String groupName, PagerPaginationSupport usersPps) {
        setSessionProperty( getUsersPpsKey(spaceKey, groupName), usersPps);
        log.debug("Set users instance " + id(usersPps) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
    }

    private String getUsersPpsKey(String spaceKey, String groupName) {
        return PLUGIN_SESSION_KEY_PREFIX + ":" + spaceKey + ":" + groupName + ":" + USERS_SESSION_KEY_SUFFIX;
    }

    public PagerPaginationSupport getSearchResultUsersPps(String spaceKey, String groupName) {
        PagerPaginationSupport result = (PagerPaginationSupport)getSessionProperty( getSearchResultUsersPpsKey(spaceKey, groupName) );
        log.debug("Got search result users instance " + id(result) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
        return result;
    }

    public void setSearchResultUsersPps(String spaceKey, String groupName, PagerPaginationSupport searchResultUsersPps) {
        setSessionProperty( getSearchResultUsersPpsKey(spaceKey, groupName), searchResultUsersPps);
        log.debug("Set search result users instance " + id(searchResultUsersPps) + " for spaceKey '" + spaceKey + "' and groupName '" + groupName + "'");
    }

    private String getSearchResultUsersPpsKey(String spaceKey, String groupName) {
        return PLUGIN_SESSION_KEY_PREFIX + ":" + spaceKey + ":" + groupName + ":" + SEARCH_RESULT_USERS_SESSION_KEY_SUFFIX;
    }

    public void clearCache() {
        log.debug("Clearing all cache (removing all session data with keys that start with '" + PLUGIN_SESSION_KEY_PREFIX + "-')");
        Map session = (Map) ActionContext.getContext().get("session");
        Iterator iter = session.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            if (key.startsWith(PLUGIN_SESSION_KEY_PREFIX)) {
                session.remove(key);
            }
        }
    }

    public void clearGroupCache(String spaceKey) {
        String sessionKey = getGroupsPpsKey(spaceKey);
        log.debug("Clearing all groups cache for spacekey '" + spaceKey + "' (removing session data for '" + sessionKey + ")");

        Map session = (Map) ActionContext.getContext().get("session");
        session.remove(sessionKey);
    }

    public void clearUserCache(String spaceKey, List groupNames) {        
        Map session = (Map) ActionContext.getContext().get("session");
        for (int i=0; i<groupNames.size(); i++) {
            String groupName = (String)groupNames.get(i);
            String sessionKey = getUsersPpsKey(spaceKey, groupName);
            log.debug("Clearing all users cache for spacekey '" + spaceKey + "' and groupName '" + groupName + "' (removing all session data for key '" + sessionKey + "')");
            session.remove(sessionKey);
        }
    }

    public void clearSearchResultUserCache(String spaceKey, List groupNames) {
        Map session = (Map) ActionContext.getContext().get("session");
        for (int i=0; i<groupNames.size(); i++) {
            String groupName = (String)groupNames.get(i);
            String sessionKey = getSearchResultUsersPpsKey(spaceKey, groupName);
            log.debug("Clearing all search result users cache for spacekey '" + spaceKey + "' and groupName '" + groupName + "' (removing all session data for key '" + sessionKey + "')");
            session.remove(sessionKey);
        }
    }
}
