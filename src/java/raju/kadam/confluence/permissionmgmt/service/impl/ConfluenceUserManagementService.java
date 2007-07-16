package raju.kadam.confluence.permissionmgmt.service.impl;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.user.search.page.DefaultPager;
import com.atlassian.user.search.query.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.displaytag.pagination.PaginatedList;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.service.*;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;
import raju.kadam.confluence.permissionmgmt.util.UserUtil;
import raju.kadam.confluence.permissionmgmt.paging.PagerPaginatedList;
import raju.kadam.confluence.permissionmgmt.paging.ListPaginatedList;
import raju.kadam.confluence.permissionmgmt.paging.LazyLoadingUserByUsernamePager;
import raju.kadam.util.LDAP.LDAPUser;
import raju.kadam.util.LDAP.LDAPUtil;
import raju.kadam.util.ListUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:48:36 AM
 */
public class ConfluenceUserManagementService implements UserManagementService {

    private UserAccessor userAccessor;
    private CustomPermissionConfiguration customPermissionConfiguration;
    private Log log = LogFactory.getLog(this.getClass());

    public ConfluenceUserManagementService() {
        log.debug("ConfluenceUserManagementService start constructor");
        userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
        //customPermissionConfiguration = (CustomPermissionConfiguration) ConfluenceUtil.loadComponentWithRetry("customPermissionConfiguration");
        log.debug("ConfluenceUserManagementService end constructor");
    }

    private List findIntersection( List existingUsersList, List returnedUsers, boolean ranQueryAtLeastOnce) {
        List users = null;
        if (ranQueryAtLeastOnce) {
            users = UserUtil.findIntersectionOfUsers(existingUsersList, returnedUsers);
        }
        else {
            users = returnedUsers;
        }

        return users;
    }

    public AdvancedUserQueryResults findUsers(AdvancedUserQuery advancedUserQuery, ServiceContext context) throws FindException {
        log.debug("findUsers() called.");
        AdvancedUserQueryResults results = new AdvancedUserQueryResults();

        Pager pager = new DefaultPager(new ArrayList());
        if (advancedUserQuery.isFullnameSearchDefined()) {
            try {
                UserNameTermQuery query = new UserNameTermQuery(advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType());
                SearchResult result = userAccessor.findUsers(query);
                pager = result.pager();
                results.setMessage("" + PagerUtils.count(pager) + " returned");
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
                results.setMessage("" + PagerUtils.count(pager) + " returned");
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
                results.setMessage("" + PagerUtils.count(pager) + " returned");
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

        if (advancedUserQuery.isGroupnameSearchDefined()) {
            try {
                GroupNameTermQuery query = new GroupNameTermQuery(advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType());
                SearchResult result = userAccessor.findGroups(query);
                List groups = PagerUtils.toList(result.pager());
                ArrayList returnedUsers = new ArrayList();
                for (int i = 0; i < groups.size(); i++) {
                    Group group = (Group) groups.get(i);
                    Pager usernamePager = userAccessor.getMemberNames(group);
                    // TODO: this is inefficient. probably should only return first 100, or maybe just axe the ability to do this query
                    List usernames = PagerUtils.toList(usernamePager);
                    for (int j=0; j<usernames.size(); j++) {
                        String username = (String)usernames.get(j);
                        User user = userAccessor.getUser(username);
                        returnedUsers.add(user);
                    }
                }
                pager = new DefaultPager(returnedUsers);
                results.setMessage("" + PagerUtils.count(pager) + " returned");
            }
            catch (EntityException e) {
                log.warn("query by groupname failed due to EntityException", e);
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
            e.printStackTrace();
        }

        return pager;
    }

    public void addUsersByUsernameToGroup(List userNames, String groupName, ServiceContext context) throws AddException {
        log.debug("addUsersByUsernameToGroup() called.");
        List groupNames = ListUtil.createListOfOneItem(groupName);
        addUsersByUsernameToGroupsByGroupname(userNames, groupNames);
    }

    private void addUsersByUsernameToGroupsByGroupname(List userNames, List groupNames) throws AddException {
        log.debug("addUsersByUsernameToGroupsByGroupname() called.");
        CustomPermissionConfiguration config = getCustomPermissionConfiguration();

        AddException ex = null;

        boolean isLDAPPresent = config.getLdapAuthUsed().equals(CustomPermissionConfigConstants.YES) ? true : false;

        //Associate selected user-groups to all users.
        for (Iterator itr = userNames.iterator(); itr.hasNext();) {
            //First check if given user is present or not
            String userid = (String) itr.next();
            User currUser = userAccessor.getUser(userid);
            if (currUser == null) {
                //create an user
                //userid doesn't exists, if LDAP present then we will create User if it exists in LDAP.
                if (isLDAPPresent) {
                    //create an user.

                    // TODO: the option to create users if they don't exist using LDAP info should be in config

                    // TODO: consider adding option and ability to create users if they don't exist, even if LDAP not used

                    currUser = createConfUser(userid, isLDAPPresent, config.getCompanyLDAPUrl(), config.getCompanyLDAPBaseDN());
                }

                //if user details not found in LDAP too, then retun userid in errorids
                if (currUser == null) {

                    if (ex == null) {
                        ex = new AddException(ErrorReason.USER_NOT_FOUND);
                    }

                    //for some reason we are unable to create user.
                    //add it to our notCreatedUser List.
                    ex.addId(userid);

                    continue;

                } else {
                    //Add this user to default group confluence-users
                    userAccessor.addMembership("confluence-users", userid);
                }
            }

            //If user exists then associate him/her to all selected usergroups
            if (currUser != null) {
                //Associate this user to all selected user-groups
                for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                    userAccessor.addMembership((String) iterator.next(), userid);
                }

            }
        }

        // If we failed, throw exception
        if (ex != null) {
            throw ex;
        }
    }

    //This method will be used to create an user when Confluence is used for Managing Wiki Users
    private User createConfUser(String creationUserName, boolean isLDAPAvailable, String companyLDAPUrl, String companyLDAPBaseDN) {
        log.debug("createConfUser() called.");
        User vUser = null;
        LDAPUser lUser = null;

        log.debug("create a confluence user -> " + creationUserName);

        try {
            //if LDAP Lookup is available, get information from there.
            if (isLDAPAvailable) {
                //log.debug("LDAP Lookup available");
                //Get user details from LDAP.
                lUser = LDAPUtil.getLDAPUser(creationUserName, companyLDAPUrl, companyLDAPBaseDN);
                if (lUser != null) {
                    vUser = userAccessor.addUser(creationUserName, creationUserName, lUser.getEmail(), lUser.getFullName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vUser;
    }


    public void removeUsersByUsernameFromGroup(List userNames, String groupName, ServiceContext context) throws RemoveException {
        log.debug("removeUsersByUsernameFromGroup() called.");
        List groupNames = ListUtil.createListOfOneItem(groupName);
        removeUsersByUsernamesFromGroupsByGroupname(userNames, groupNames);
    }

    private void removeUsersByUsernamesFromGroupsByGroupname(List userNames, List groupNames) {
        log.debug("removeUsersByUsernamesFromGroupsByGroupname() called.");
        for (Iterator itr = userNames.iterator(); itr.hasNext();) {
            String userid = (String) itr.next();
            for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                userAccessor.removeMembership((String) iterator.next(), userid);
            }
        }
    }

    public boolean isMemberOf(String userName, String groupName) {
        log.debug("isMemberOf() called.");
        boolean result = false;
        Group group = userAccessor.getGroup(groupName);
        if (group!=null) {
            Pager pager = userAccessor.getMemberNames(group);
            List memberNames = PagerUtils.toList(pager);
            if (memberNames!=null) {
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
