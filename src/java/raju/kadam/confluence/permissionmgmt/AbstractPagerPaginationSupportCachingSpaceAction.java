package raju.kadam.confluence.permissionmgmt;

import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAdministrative;

import java.util.Map;
import java.util.HashMap;

import bucket.core.actions.PagerPaginationSupport;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 12, 2007
 * Time: 4:10:24 PM
 */
public abstract class AbstractPagerPaginationSupportCachingSpaceAction extends AbstractSpaceAction implements SpaceAdministrative {

    private Map keyToGroupsPps = new HashMap();
    private Map keyToKeyToUsersPps = new HashMap();

    public PagerPaginationSupport getGroupsPps(String spaceKey) {
        return (PagerPaginationSupport)keyToGroupsPps.get(spaceKey);
    }

    public void setGroupsPps(String spaceKey, PagerPaginationSupport groupsPps) {
        if (spaceKey != null) {
            if (groupsPps != null) {
                this.keyToGroupsPps.put(spaceKey, groupsPps);
            }
            else {
                this.keyToGroupsPps.remove(spaceKey);
            }
        }
    }

    public PagerPaginationSupport getUsersPps(String spaceKey, String groupName) {
        PagerPaginationSupport pps = null;
        Map typeToMap = this.keyToKeyToUsersPps;
        if ( spaceKey != null && groupName != null ) {
            Map keyToPPSMap = getOrMakeMap(spaceKey, typeToMap);
            pps = (PagerPaginationSupport)keyToPPSMap.get(groupName);
        }

        return pps;
    }

    public void setUsersPps(String spaceKey, String groupName, PagerPaginationSupport usersPps) {
        Map typeToMap = this.keyToKeyToUsersPps;
        if ( spaceKey != null && key != null ) {
            Map keyToPPSMap = getOrMakeMap(spaceKey, typeToMap);
            if ( usersPps != null ) {
                keyToPPSMap.put(groupName, usersPps);
            }
            else {
                keyToPPSMap.remove(groupName);
            }
        }
    }

    private Map getOrMakeMap( String type, Map mapToGetFrom ) {
        Map gottenMap = (Map)mapToGetFrom.get(type);
        if (gottenMap == null) {
            gottenMap = new HashMap();
            mapToGetFrom.put(type, gottenMap);
        }
        return gottenMap;
    }

    public void clearGroupAndUserCache() {
        this.keyToGroupsPps = new HashMap();
        this.keyToKeyToUsersPps = new HashMap();
    }
}
