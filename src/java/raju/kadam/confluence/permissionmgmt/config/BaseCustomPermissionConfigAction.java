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

package raju.kadam.confluence.permissionmgmt.config;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.core.Administrative;

/**
 * @author Gary S. Weaver
 */
public class BaseCustomPermissionConfigAction extends ConfluenceActionSupport implements Administrative, CustomPermissionConfigurable {

    private String userManagerLocation;
    private String ldapAuthUsed;
    private String maxUserIDsLimit;
    private String maxGroupIDsLimit;
    private String userGroupsMatchingPattern;
    private String pluginDown;
    private String downTimeMessage;
    private String groupActionsPermitted;
    private String newGroupNameCreationPrefixPattern;
    private String newGroupNameCreationSuffixPattern;
    private String userSearchEnabled;

    public String getUserManagerLocation() {
        return userManagerLocation;
    }

    public void setUserManagerLocation(String userManagerLocation) {
        this.userManagerLocation = userManagerLocation;
    }

    public String getLdapAuthUsed() {
        return ldapAuthUsed;
    }

    public void setLdapAuthUsed(String ldapAuthUsed) {
        this.ldapAuthUsed = ldapAuthUsed;
    }

    public String getMaxUserIDsLimit() {
        return maxUserIDsLimit;
    }

    public void setMaxUserIDsLimit(String maxUserIDsLimit) {
        this.maxUserIDsLimit = maxUserIDsLimit;
    }

    public String getMaxGroupIDsLimit() {
        return maxGroupIDsLimit;
    }

    public void setMaxGroupIDsLimit(String maxGroupIDsLimit) {
        this.maxGroupIDsLimit = maxGroupIDsLimit;
    }

    public String getUserGroupsMatchingPattern() {
        return userGroupsMatchingPattern;
    }

    public void setUserGroupsMatchingPattern(String userGroupsMatchingPattern) {
        this.userGroupsMatchingPattern = userGroupsMatchingPattern;
    }

    public String getPluginDown() {
        return pluginDown;
    }

    public void setPluginDown(String pluginDown) {
        this.pluginDown = pluginDown;
    }

    public String getDownTimeMessage() {
        return downTimeMessage;
    }

    public void setDownTimeMessage(String downTimeMessage) {
        this.downTimeMessage = downTimeMessage;
    }

    public String getGroupActionsPermitted() {
        return groupActionsPermitted;
    }

    public void setGroupActionsPermitted(String groupActionsPermitted) {
        this.groupActionsPermitted = groupActionsPermitted;
    }

    public String getNewGroupNameCreationPrefixPattern() {
        return newGroupNameCreationPrefixPattern;
    }

    public void setNewGroupNameCreationPrefixPattern(String newGroupNameCreationPrefixPattern) {
        this.newGroupNameCreationPrefixPattern = newGroupNameCreationPrefixPattern;
    }

    public String getNewGroupNameCreationSuffixPattern() {
        return newGroupNameCreationSuffixPattern;
    }

    public void setNewGroupNameCreationSuffixPattern(String newGroupNameCreationSuffixPattern) {
        this.newGroupNameCreationSuffixPattern = newGroupNameCreationSuffixPattern;
    }

    public String getUserSearchEnabled() {
        return userSearchEnabled;
    }

    public void setUserSearchEnabled(String userSearchEnabled) {
        this.userSearchEnabled = userSearchEnabled;
    }
}
