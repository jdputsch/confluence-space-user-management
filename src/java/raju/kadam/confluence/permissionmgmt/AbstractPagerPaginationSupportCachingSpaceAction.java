package raju.kadam.confluence.permissionmgmt;

import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAdministrative;
import com.opensymphony.xwork.ActionContext;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

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

    public PagerPaginationSupport getGroupsPps(String spaceKey) {

        return (PagerPaginationSupport)getSessionProperty( PLUGIN_SESSION_KEY_PREFIX + "-" + spaceKey + "-" + GROUPS_SESSION_KEY_SUFFIX );
    }

    public void setGroupsPps(String spaceKey, PagerPaginationSupport groupsPps) {
        setSessionProperty( PLUGIN_SESSION_KEY_PREFIX + "-" + spaceKey + "-" + GROUPS_SESSION_KEY_SUFFIX, groupsPps);
    }

    public PagerPaginationSupport getUsersPps(String spaceKey, String groupName) {
        return (PagerPaginationSupport)getSessionProperty( PLUGIN_SESSION_KEY_PREFIX + "-" + spaceKey + "-" + groupName + "-" + USERS_SESSION_KEY_SUFFIX );
    }

    public void setUsersPps(String spaceKey, String groupName, PagerPaginationSupport usersPps) {
        setSessionProperty( PLUGIN_SESSION_KEY_PREFIX + "-" + spaceKey + "-" + groupName + "-" + USERS_SESSION_KEY_SUFFIX, usersPps);
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
        String sessionKey = PLUGIN_SESSION_KEY_PREFIX + "-" + spaceKey + "-" + GROUPS_SESSION_KEY_SUFFIX;
        log.debug("Clearing all groups cache for spacekey '" + spaceKey + "' (removing session data for '" + sessionKey + ")");

        Map session = (Map) ActionContext.getContext().get("session");
        session.remove(sessionKey);
    }

    public void clearUserCache(String spaceKey, String groupName) {
        String sessionKey = PLUGIN_SESSION_KEY_PREFIX + "-" + spaceKey + "-" + groupName + "-" + USERS_SESSION_KEY_SUFFIX;
        log.debug("Clearing all users cache for spacekey '" + spaceKey + "' and groupName '" + groupName + "' (removing all session data for key '" + sessionKey + "')");

        Map session = (Map) ActionContext.getContext().get("session");
        session.remove(sessionKey);
    }
}
