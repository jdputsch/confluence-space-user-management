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

    BootstrapManager bootstrapManager;

    String loggedInUser;
    String key;
    String adminAction;
    Map paramMap;
    List usersToAddList;
    List usersToRemoveList;
    List groupsToAddList;
    List groupsToRemoveList;
    List selectedGroups;
    String secretId;
    String spaceKey;

    public String toString() {
        return "loggedInUser=" + loggedInUser +
                ", key=" + key +
                ", adminAction=" + adminAction +
                ", paramMap=" + paramMap +
                ", usersToAddList=" + usersToAddList +
                ", usersToRemoveList=" + usersToRemoveList +
                ", groupsToAddList=" + groupsToAddList +
                ", groupsToRemoveList=" + groupsToRemoveList +
                ", selectedGroups=" + selectedGroups;
    }


    public BootstrapManager getBootstrapManager() {
        return bootstrapManager;
    }

    public void setBootstrapManager(BootstrapManager bootstrapManager) {
        this.bootstrapManager = bootstrapManager;
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

    public Map getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map paramMap) {
        this.paramMap = paramMap;
    }

    public List getUsersToAddList() {
        return usersToAddList;
    }

    public void setUsersToAddList(List usersToAddList) {
        this.usersToAddList = usersToAddList;
    }

    public List getUsersToRemoveList() {
        return usersToRemoveList;
    }

    public void setUsersToRemoveList(List usersToRemoveList) {
        this.usersToRemoveList = usersToRemoveList;
    }

    public List getGroupsToAddList() {
        return groupsToAddList;
    }

    public void setGroupsToAddList(List groupsToAddList) {
        this.groupsToAddList = groupsToAddList;
    }

    public List getGroupsToRemoveList() {
        return groupsToRemoveList;
    }

    public void setGroupsToRemoveList(List groupsToRemoveList) {
        this.groupsToRemoveList = groupsToRemoveList;
    }

    public List getSelectedGroups() {
        return selectedGroups;
    }

    public void setSelectedGroups(List selectedGroups) {
        this.selectedGroups = selectedGroups;
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }
}
