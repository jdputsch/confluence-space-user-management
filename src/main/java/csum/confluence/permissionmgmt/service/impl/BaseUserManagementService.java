package csum.confluence.permissionmgmt.service.impl;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.DefaultPager;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.user.search.query.EmailTermQuery;
import com.atlassian.user.search.query.FullNameTermQuery;
import com.atlassian.user.search.query.TermQuery;
import com.atlassian.user.search.query.UserNameTermQuery;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.service.UserManagementService;
import csum.confluence.permissionmgmt.service.exception.FindException;
import csum.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import csum.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.ldap.LDAPException;
import csum.confluence.permissionmgmt.util.ldap.LDAPLookup;
import csum.confluence.permissionmgmt.util.ldap.LDAPUser;
import csum.confluence.permissionmgmt.util.ldap.LDAPHelper;
import csum.confluence.permissionmgmt.util.ldap.osuser.OSUserParser;
import csum.confluence.permissionmgmt.util.paging.LazyLoadingUserByUsernamePager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 6, 2007
 * Time: 11:37:55 AM
 */
public abstract class BaseUserManagementService implements UserManagementService {

    protected UserAccessor userAccessor;
    private CustomPermissionConfiguration customPermissionConfiguration;
    protected Log log = LogFactory.getLog(this.getClass());

    public BaseUserManagementService() {
        log.debug("BaseUserManagementService start constructor");
        userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
        //customPermissionConfiguration = (CustomPermissionConfiguration) ConfluenceUtil.loadComponentWithRetry("customPermissionConfiguration");
        log.debug("BaseUserManagementService end constructor");
    }

    protected LDAPUser getLDAPUser(String userid) throws ParserConfigurationException, LDAPException {
        return LDAPHelper.getLDAPUser(getCustomPermissionConfiguration(), userid);
    }

    public AdvancedUserQueryResults findUsers(AdvancedUserQuery advancedUserQuery, ServiceContext context) throws FindException {
        log.debug("findUsers() called.");
        AdvancedUserQueryResults results = new AdvancedUserQueryResults();

        //TODO: this is really slow with osuser search. must use http://confluence.atlassian.com/display/DOC/How+to+Improve+User+Search+Performance

        Pager pager = new DefaultPager(new ArrayList());
        if (advancedUserQuery.isUsernameSearchDefined()) {
            try {
                UserNameTermQuery query = new UserNameTermQuery(advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType());
                SearchResult result = userAccessor.findUsers(query);
                pager = result.pager();
                //results.setMessage("" + PagerUtils.count(pager) + " returned");
            }
            catch (EntityException e) {
                log.warn("query by username failed due to EntityException", e);
                results.setMessage("" + e);
            }
            catch (IllegalArgumentException e) {
                // if search type is not allowed
                log.warn("Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'", e);
                results.setMessage("Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'");
            }
        }

        if (advancedUserQuery.isFullnameSearchDefined()) {
            try {
                FullNameTermQuery query = new FullNameTermQuery(advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType());
                SearchResult result = userAccessor.findUsers(query);
                pager = result.pager();
                //results.setMessage("" + PagerUtils.count(pager) + " returned");
            }
            catch (EntityException e) {
                log.warn("query by user fullname failed due to EntityException", e);
                results.setMessage("" + e);
            }
            catch (IllegalArgumentException e) {
                // if search type is not allowed
                log.warn("Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'", e);
                results.setMessage("Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'");
            }
        }

        if (advancedUserQuery.isEmailSearchDefined()) {
            try {
                EmailTermQuery query = new EmailTermQuery(advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType());
                SearchResult result = userAccessor.findUsers(query);
                pager = result.pager();
                //results.setMessage("" + PagerUtils.count(pager) + " returned");
            }
            catch (EntityException e) {
                log.warn("query by user email failed due to EntityException", e);
                results.setMessage("" + e);
            }
            catch (IllegalArgumentException e) {
                // if search type is not allowed
                log.warn("Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'", e);
                results.setMessage("Bad value '" + advancedUserQuery.getPartialSearchTerm() + "' for search type '" + advancedUserQuery.getSubstringMatchType() + "'");
            }
        }

        results.setUsers(pager);

        return results;
    }

    public Pager findUsersForGroup(String groupName, ServiceContext context) {
        log.debug("findUsersForGroup(groupName) called. groupName='" + groupName + "'");
        Group group = userAccessor.getGroup(groupName);
        return findUsersForGroup(group);
    }

    private Pager findUsersForGroup(Group group) {
        log.debug("findUsersForGroup(Group) called.");
        Pager usernamePager = userAccessor.getMemberNames(group);
        LazyLoadingUserByUsernamePager userPager = new LazyLoadingUserByUsernamePager();
        userPager.setUsernamePager(usernamePager);
        userPager.setUserAccessor(this.userAccessor);
        return userPager;
    }

    public Pager findUsersWhoseNameStartsWith(String partialName, ServiceContext context) {
        log.debug("findUsersWhoseNameStartsWith() called. partialName='" + partialName + "'");
        Pager pager = null;

        try {
            UserNameTermQuery query = new UserNameTermQuery(partialName, TermQuery.SUBSTRING_STARTS_WITH);
            SearchResult searchResult = userAccessor.findUsers(query);
            pager = searchResult.pager();
        }
        catch (EntityException e) {
            log.error("Error finding username that starts with " + partialName, e);
        }

        return pager;
    }

    protected String getErrorMessage(List usersNotFound, List groupsNotFound, ServiceContext context) {
        String msg = "";
        String concat = "";
        if (usersNotFound.size() > 0) {
            msg += context.getText("error.usersNotFound") + ": " +
                    StringUtil.convertCollectionToCommaDelimitedString(usersNotFound) + ".";
            concat = " ";
        }

        if (groupsNotFound.size() > 0) {
            msg += concat;
            msg += context.getText("error.groupsNotFound") + ": " +
                    StringUtil.convertCollectionToCommaDelimitedString(groupsNotFound) + ".";
        }

        return msg;
    }

    public boolean isMemberOf(String userName, String groupName) {
        log.debug("isMemberOf() called. userName=" + userName + " groupName=" + groupName);
        boolean result = false;
        Group group = userAccessor.getGroup(groupName);
        if (group != null) {
            Pager pager = userAccessor.getMemberNames(group);
            List memberNames = PagerUtils.toList(pager);
            if (memberNames != null) {
                result = memberNames.contains(userName);
            }
        }
        return result;
    }

    public UserAccessor getUserAccessor() {
        return userAccessor;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }

}
