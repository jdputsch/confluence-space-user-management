/**
* Copyright (c) 2007, Custom Space Usergroups Manager Development Team
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the Custom Space Usergroups Manager Development Team nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE CUSTOM SPACE USERGROUPS MANAGER DEVELOPMENT TEAM ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE CUSTOM SPACE USERGROUPS MANAGER DEVELOPMENT TEAM BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package raju.kadam.confluence.permissionmgmt;

import raju.kadam.confluence.permissionmgmt.util.StringUtil;

import java.util.List;

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
