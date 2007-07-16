package raju.kadam.confluence.permissionmgmt;

import raju.kadam.util.StringUtil;

import java.util.List;
import java.util.Map;

import com.atlassian.confluence.setup.BootstrapManager;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 19, 2007
 * Time: 2:45:44 PM
 */
public class CustomPermissionManagerActionContext {

    String loggedInUser;
    String key;
    String adminAction;
    List specifiedUsers;
    List specifiedGroups;
    String userSearch;

    public String toString() {
        return "loggedInUser=" + loggedInUser +
                ", key=" + key +
                ", adminAction=" + adminAction +
                ", specifiedUsers=" + StringUtil.convertCollectionToCommaDelimitedString(specifiedUsers) +
                ", specifiedGroups=" + StringUtil.convertCollectionToCommaDelimitedString(specifiedGroups) +
                ", userSearch=" + userSearch;
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(String loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAdminAction() {
        return adminAction;
    }

    public void setAdminAction(String adminAction) {
        this.adminAction = adminAction;
    }

    public List getSpecifiedUsers() {
        return specifiedUsers;
    }

    public void setSpecifiedUsers(List specifiedUsers) {
        this.specifiedUsers = specifiedUsers;
    }

    public List getSpecifiedGroups() {
        return specifiedGroups;
    }

    public void setSpecifiedGroups(List specifiedGroups) {
        this.specifiedGroups = specifiedGroups;
    }

    public String getUserSearch() {
        return userSearch;
    }

    public void setUserSearch(String userSearch) {
        this.userSearch = userSearch;
    }
}
