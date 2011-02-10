/**
 * Copyright (c) 2007-2011, Custom Space User Management Plugin Development Team
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
 *     * Neither the name of the Custom Space User Management Plugin Development Team
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

package csum.confluence.permissionmgmt.config;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Gary S. Weaver
 */
public class BaseCustomPermissionConfigAction extends ConfluenceActionSupport implements CustomPermissionConfigurable {

    protected Log log = LogFactory.getLog(this.getClass());

    private String userManagerLocation;
    private String ldapAuthUsed;
    private String jiraSoapUrl;
    private String jiraSoapUsername;
    private String jiraSoapPassword;
    private String maxUserIDsLimit;
    private String maxGroupIDsLimit;
    private String pluginDown;
    private String downTimeMessage;
    private String groupActionsPermitted;
    private String newGroupNameCreationPrefixPattern;
    private String newGroupNameCreationSuffixPattern;
    private String userSearchEnabled;
    private String providerType;
    private String ldapUserIdAttribute;
    private String ldapEmailAttribute;
    private String ldapFirstNameAttribute;
    private String ldapLastNameAttribute;
    private String ldapProviderFullyQualifiedClassname;
    private String userFullNameFormat;
    private String ldapConfigTestUsername;
    private String ldapNarrowingFilterExpression;
    private String personalSpaceAllowed;
    private String groupMembershipRefreshFixEnabled;
    private String numRowsPerPage;
    private String unvalidatedUserAdditionEnabled;

    public BaseCustomPermissionConfigAction() {
        log.debug("instantiating BaseCustomPermissionConfigAction");
        log.debug("instantiated BaseCustomPermissionConfigAction");
    }

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

    public String getJiraSoapUrl() {
        return jiraSoapUrl;
    }

    public void setJiraSoapUrl(String jiraSoapUrl) {
        this.jiraSoapUrl = jiraSoapUrl;
    }

    public String getJiraSoapUsername() {
        return jiraSoapUsername;
    }

    public void setJiraSoapUsername(String jiraSoapUsername) {
        this.jiraSoapUsername = jiraSoapUsername;
    }

    public String getJiraSoapPassword() {
        return jiraSoapPassword;
    }

    public void setJiraSoapPassword(String jiraSoapPassword) {
        this.jiraSoapPassword = jiraSoapPassword;
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

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getLdapUserIdAttribute() {
        return ldapUserIdAttribute;
    }

    public void setLdapUserIdAttribute(String ldapUserIdAttribute) {
        this.ldapUserIdAttribute = ldapUserIdAttribute;
    }

    public String getLdapEmailAttribute() {
        return ldapEmailAttribute;
    }

    public void setLdapEmailAttribute(String ldapEmailAttribute) {
        this.ldapEmailAttribute = ldapEmailAttribute;
    }

    public String getLdapFirstNameAttribute() {
        return ldapFirstNameAttribute;
    }

    public void setLdapFirstNameAttribute(String ldapFirstNameAttribute) {
        this.ldapFirstNameAttribute = ldapFirstNameAttribute;
    }

    public String getLdapLastNameAttribute() {
        return ldapLastNameAttribute;
    }

    public void setLdapLastNameAttribute(String ldapLastNameAttribute) {
        this.ldapLastNameAttribute = ldapLastNameAttribute;
    }

    public String getLdapProviderFullyQualifiedClassname() {
        return ldapProviderFullyQualifiedClassname;
    }

    public void setLdapProviderFullyQualifiedClassname(String ldapProviderFullyQualifiedClassname) {
        this.ldapProviderFullyQualifiedClassname = ldapProviderFullyQualifiedClassname;
    }

    public String getUserFullNameFormat() {
        return userFullNameFormat;
    }

    public void setUserFullNameFormat(String userFullNameFormat) {
        this.userFullNameFormat = userFullNameFormat;
    }

    public String getLdapConfigTestUsername() {
        return ldapConfigTestUsername;
    }

    public void setLdapConfigTestUsername(String ldapConfigTestUsername) {
        this.ldapConfigTestUsername = ldapConfigTestUsername;
    }

    public String getLdapNarrowingFilterExpression() {
        return ldapNarrowingFilterExpression;
    }

    public void setLdapNarrowingFilterExpression(String ldapNarrowingFilterExpression) {
        this.ldapNarrowingFilterExpression = ldapNarrowingFilterExpression;
    }

    public String getPersonalSpaceAllowed() {
        return personalSpaceAllowed;
    }

    public void setPersonalSpaceAllowed(String personalSpaceAllowed) {
        this.personalSpaceAllowed = personalSpaceAllowed;
    }

    public String getGroupMembershipRefreshFixEnabled() {
        return groupMembershipRefreshFixEnabled;
    }

    public void setGroupMembershipRefreshFixEnabled(String groupMembershipRefreshFixEnabled) {
        this.groupMembershipRefreshFixEnabled = groupMembershipRefreshFixEnabled;
    }
    
    public String getNumRowsPerPage() {
        return numRowsPerPage;
    }

    public void setNumRowsPerPage(String numRowsPerPage) {
        this.numRowsPerPage = numRowsPerPage;
    }

    public String getUnvalidatedUserAdditionEnabled() {
        return unvalidatedUserAdditionEnabled;
    }

    public void setUnvalidatedUserAdditionEnabled(String unvalidatedUserAdditionEnabled) {
        this.unvalidatedUserAdditionEnabled = unvalidatedUserAdditionEnabled;
    }
}
