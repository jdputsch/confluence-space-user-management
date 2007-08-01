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

package raju.kadam.confluence.permissionmgmt.service.vo;

import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuerySubstringMatchType;
import raju.kadam.confluence.permissionmgmt.util.ConfigUtil;

/**
 * @author Gary S. Weaver
 */
public class AdvancedUserQuery {

    private String lookupType = AdvancedUserQueryLookupType.USERNAME;
    private String partialSearchTerm;
    private String substringMatchType = AdvancedUserQuerySubstringMatchType.SUBSTRING_STARTS_WITH;

    public String toString() {
        return "lookupType=" + lookupType + " partialSearchTerm=" + partialSearchTerm + " substringMatchType=" + substringMatchType;
    }

    public String getLookupType() {
        return lookupType;
    }

    public void setLookupType(String lookupType) {
        this.lookupType = lookupTypeInstance(lookupType);
    }

    public String getPartialSearchTerm() {
        return partialSearchTerm;
    }

    public void setPartialSearchTerm(String partialSearchTerm) {
        this.partialSearchTerm = partialSearchTerm;
    }

    public String getSubstringMatchType() {
        return substringMatchType;
    }

    public void setSubstringMatchType(String substringMatchType) {
        this.substringMatchType = substringMatchTypeInstance(substringMatchType);
    }

    public boolean isDefined() {
        boolean result = false;
        if ( this.getLookupType() != null ||
                this.getSubstringMatchType() != null ||
                !ConfigUtil.isNullOrEmpty(this.getPartialSearchTerm())) {
            result = true;
        }
        return result;
    }

    // TODO: consider using enumeration instance instead
    public String lookupTypeInstance(String s) {
        String result = null;
        if (s!=null) {
            if (AdvancedUserQueryLookupType.USERNAME.equalsIgnoreCase(s) ) {
                result = AdvancedUserQueryLookupType.USERNAME;
            }
            else if (AdvancedUserQueryLookupType.USER_FULL_NAME.equalsIgnoreCase(s) ) {
                result = AdvancedUserQueryLookupType.USER_FULL_NAME;
            }
            else if (AdvancedUserQueryLookupType.USER_EMAIL.equalsIgnoreCase(s) ) {
                result = AdvancedUserQueryLookupType.USER_EMAIL;
            }
        }
        return result;
    }

    // note: atlassian-user is really picky about doing == instead of .equals to check the TermQuery constant, so we do this
    public String substringMatchTypeInstance(String s) {
        String result = null;
        if (s!=null) {
            if (AdvancedUserQuerySubstringMatchType.SUBSTRING_CONTAINS.equalsIgnoreCase(s) ) {
                result = AdvancedUserQuerySubstringMatchType.SUBSTRING_CONTAINS;
            }
            else if (AdvancedUserQuerySubstringMatchType.SUBSTRING_ENDS_WITH.equalsIgnoreCase(s) ) {
                result = AdvancedUserQuerySubstringMatchType.SUBSTRING_ENDS_WITH;
            }
            else if (AdvancedUserQuerySubstringMatchType.SUBSTRING_STARTS_WITH.equalsIgnoreCase(s) ) {
                result = AdvancedUserQuerySubstringMatchType.SUBSTRING_STARTS_WITH;
            }
        }
        return result;
    }

    public boolean isUsernameSearchDefined() {
        boolean result = false;
        if (this.getLookupType()==AdvancedUserQueryLookupType.USERNAME) {
            result = true;
        }
        return result;
    }

    public boolean isFullnameSearchDefined() {
        boolean result = false;
        if (this.getLookupType()==AdvancedUserQueryLookupType.USER_FULL_NAME) {
            result = true;
        }
        return result;
    }

    public boolean isEmailSearchDefined() {
        boolean result = false;
        if (this.getLookupType()==AdvancedUserQueryLookupType.USER_EMAIL) {
            result = true;
        }
        return result;
    }

    public boolean isValid()
    {
        return isDefined();
    }
}
