package raju.kadam.confluence.permissionmgmt.service.vo;

import java.util.List;
import java.util.ArrayList;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 3, 2007
 * Time: 2:00:39 PM
 */
public class AdvancedUserQueryResults {

    private List users = new ArrayList();

    private String userNameFieldMessage;
    private String fullNameFieldMessage;
    private String emailFieldMessage;
    private String groupNameFieldMessage;


    public List getUsers() {
        return users;
    }

    public void setUsers(List users) {
        this.users = users;
    }

    public String getUserNameFieldMessage() {
        return userNameFieldMessage;
    }

    public void setUserNameFieldMessage(String userNameFieldMessage) {
        this.userNameFieldMessage = userNameFieldMessage;
    }

    public String getFullNameFieldMessage() {
        return fullNameFieldMessage;
    }

    public void setFullNameFieldMessage(String fullNameFieldMessage) {
        this.fullNameFieldMessage = fullNameFieldMessage;
    }

    public String getEmailFieldMessage() {
        return emailFieldMessage;
    }

    public void setEmailFieldMessage(String emailFieldMessage) {
        this.emailFieldMessage = emailFieldMessage;
    }

    public String getGroupNameFieldMessage() {
        return groupNameFieldMessage;
    }

    public void setGroupNameFieldMessage(String groupNameFieldMessage) {
        this.groupNameFieldMessage = groupNameFieldMessage;
    }
}
