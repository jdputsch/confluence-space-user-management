package raju.kadam.confluence.permissionmgmt.service.vo;

import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuerySubstringMatchType;
import raju.kadam.util.ConfigUtil;

import com.atlassian.user.search.query.TermQuery;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 21, 2007
 * Time: 11:34:18 AM
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
