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
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 12, 2007
 * Time: 4:10:24 PM
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
