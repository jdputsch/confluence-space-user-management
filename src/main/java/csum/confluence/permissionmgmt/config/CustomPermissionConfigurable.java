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

package csum.confluence.permissionmgmt.config;

/**
 * @author Gary S. Weaver
 */
public interface CustomPermissionConfigurable {

    public String getUserManagerLocation();
    public void setUserManagerLocation(String userManagerLocation);

    public String getLdapAuthUsed();
    public void setLdapAuthUsed(String ldapAuthUsed);

    public String getMaxUserIDsLimit();
    public void setMaxUserIDsLimit(String maxUserIDsLimit);

    public String getMaxGroupIDsLimit();
    public void setMaxGroupIDsLimit(String maxGroupIDsLimit);

    public String getUserGroupsMatchingPattern();
    public void setUserGroupsMatchingPattern(String userGroupsMatchingPattern);

    public String getPluginDown();
    public void setPluginDown(String pluginDown);
    
    public String getDownTimeMessage();
    public void setDownTimeMessage(String downTimeMessage);

    public String getGroupActionsPermitted();
    public void setGroupActionsPermitted(String groupActionsPermitted);

    public String getNewGroupNameCreationPrefixPattern();
    public void setNewGroupNameCreationPrefixPattern(String newGroupNameCreationPrefixPattern);

    public String getNewGroupNameCreationSuffixPattern();
    public void setNewGroupNameCreationSuffixPattern(String newGroupNameCreationSuffixPattern);

    public String getUserSearchEnabled();
    public void setUserSearchEnabled(String userSearchEnabled);

    public String getJiraSoapUrl();
    public void setJiraSoapUrl(String jiraSoapUrl);

    public String getJiraSoapUsername();
    public void setJiraSoapUsername(String jiraSoapUsername);

    public String getJiraSoapPassword();
    public void setJiraSoapPassword(String jiraSoapPassword);

    public String getProviderType();
    public void setProviderType(String providerType);

    public String getLdapUserIdAttribute();
    public void setLdapUserIdAttribute(String ldapUserIdAttribute);

    public String getLdapEmailAttribute();
    public void setLdapEmailAttribute(String ldapEmailAttribute);

    public String getLdapNameAttribute();
    public void setLdapNameAttribute(String ldapNameAttribute);

    public String getLdapProviderFullyQualifiedClassname();
    public void setLdapProviderFullyQualifiedClassname(String ldapProviderFullyQualifiedClassname);
}
