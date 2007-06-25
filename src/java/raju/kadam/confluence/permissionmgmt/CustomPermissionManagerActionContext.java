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
    List usersToAdd;
    List usersToRemove;
    String groupToAdd;
    String groupToRemove;
    String selectedGroup;

    public String toString() {
        return "loggedInUser=" + loggedInUser +
                ", key=" + key +
                ", adminAction=" + adminAction +
                ", usersToAdd=" + StringUtil.convertCollectionToCommaDelimitedString(usersToAdd) +
                ", usersToRemove=" + StringUtil.convertCollectionToCommaDelimitedString(usersToRemove) +
                ", groupToAdd=" + groupToAdd +
                ", groupToRemove=" + groupToRemove +
                ", selectedGroup=" + selectedGroup;
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

    public List getUsersToAdd() {
        return usersToAdd;
    }

    public void setUsersToAdd(List usersToAdd) {
        this.usersToAdd = usersToAdd;
    }

    public List getUsersToRemove() {
        return usersToRemove;
    }

    public void setUsersToRemove(List usersToRemove) {
        this.usersToRemove = usersToRemove;
    }

    public String getGroupToAdd() {
        return groupToAdd;
    }

    public void setGroupToAdd(String groupToAdd) {
        this.groupToAdd = groupToAdd;
    }

    public String getGroupToRemove() {
        return groupToRemove;
    }

    public void setGroupToRemove(String groupToRemove) {
        this.groupToRemove = groupToRemove;
    }

    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(String selectedGroup) {
        this.selectedGroup = selectedGroup;
    }
}
