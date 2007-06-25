package raju.kadam.confluence.permissionmgmt.service.vo;

import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedQueryType;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 21, 2007
 * Time: 11:34:18 AM
 */
public class AdvancedUserQuery {

    private String partialUserName;
    private String userNameSearchType = AdvancedQueryType.SUBSTRING_STARTS_WITH;

    private String partialFullName;
    private String fullNameSearchType = AdvancedQueryType.SUBSTRING_STARTS_WITH;

    private String partialEmail;
    private String emailSearchType = AdvancedQueryType.SUBSTRING_STARTS_WITH;

    private String partialGroupName;
    private String groupNameSearchType = AdvancedQueryType.SUBSTRING_STARTS_WITH;


    public String getPartialUserName() {
        return partialUserName;
    }

    public void setPartialUserName(String partialUserName) {
        this.partialUserName = partialUserName;
    }

    public String getUserNameSearchType() {
        return userNameSearchType;
    }

    public void setUserNameSearchType(String userNameSearchType) {
        this.userNameSearchType = userNameSearchType;
    }

    public String getPartialFullName() {
        return partialFullName;
    }

    public void setPartialFullName(String partialFullName) {
        this.partialFullName = partialFullName;
    }

    public String getFullNameSearchType() {
        return fullNameSearchType;
    }

    public void setFullNameSearchType(String fullNameSearchType) {
        this.fullNameSearchType = fullNameSearchType;
    }

    public String getPartialEmail() {
        return partialEmail;
    }

    public void setPartialEmail(String partialEmail) {
        this.partialEmail = partialEmail;
    }

    public String getEmailSearchType() {
        return emailSearchType;
    }

    public void setEmailSearchType(String emailSearchType) {
        this.emailSearchType = emailSearchType;
    }

    public String getPartialGroupName() {
        return partialGroupName;
    }

    public void setPartialGroupName(String partialGroupName) {
        this.partialGroupName = partialGroupName;
    }

    public String getGroupNameSearchType() {
        return groupNameSearchType;
    }

    public void setGroupNameSearchType(String groupNameSearchType) {
        this.groupNameSearchType = groupNameSearchType;
    }
}
