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

package raju.kadam.confluence.permissionmgmt.service.impl;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.user.search.page.DefaultPager;
import com.atlassian.user.search.query.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.service.*;
import raju.kadam.confluence.permissionmgmt.service.exception.AddException;
import raju.kadam.confluence.permissionmgmt.service.exception.FindException;
import raju.kadam.confluence.permissionmgmt.service.exception.RemoveException;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;
import raju.kadam.confluence.permissionmgmt.util.user.UserUtil;
import raju.kadam.confluence.permissionmgmt.util.StringUtil;
import raju.kadam.confluence.permissionmgmt.util.paging.LazyLoadingUserByUsernamePager;
import raju.kadam.confluence.permissionmgmt.util.ldap.LDAPUser;
import raju.kadam.confluence.permissionmgmt.util.ldap.OSUserLDAPHelper;

import java.util.*;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
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
            e.printStackTrace();
        }

        return pager;
    }

    public void addUsersByUsernameToGroups(List userNames, List groupNames, ServiceContext context) throws AddException {
        log.debug("addUsersByUsernameToGroupsByGroupname() called. " +
                "usernames=" + StringUtil.convertCollectionToCommaDelimitedString(userNames) +
                ", groupnames=" + StringUtil.convertCollectionToCommaDelimitedString(groupNames));
        CustomPermissionConfiguration config = getCustomPermissionConfiguration();

        List usersNotFound = new ArrayList();
        // using map to get only unique groups. using treemap to keep groupnames in order
        Map groupsNotFoundMap = new TreeMap();

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

                    //for some reason we are unable to create user.
                    //add it to our notCreatedUser List.
                    usersNotFound.add(userid);

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
                    String groupName = (String) iterator.next();
                    if (groupsNotFoundMap.get(groupName)==null) {
                        // Am thoroughly confounded that the groupname here has to be lowercase. have backup check for
                        // regular (mixed-case) lookup, just in case that is a bug.
                        // TODO: test this and submit bug as needed
                        String lowercaseGroupName = groupName.toLowerCase();
                        if (userAccessor.getGroup(lowercaseGroupName)!=null) {
                            userAccessor.addMembership(lowercaseGroupName, userid);
                        }
                        else if (userAccessor.getGroup(groupName)!=null) {
                            userAccessor.addMembership(groupName, userid);
                        }
                        else {
                            groupsNotFoundMap.put( "" + groupName, "");
                        }
                    }
                }
            }
        }

        // If we failed, throw exception
        List groupsNotFound = new ArrayList(groupsNotFoundMap.keySet());
        if (usersNotFound.size()>0 || groupsNotFound.size()>0) {
            throw new AddException(getErrorMessage(usersNotFound, groupsNotFound, context));
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
                OSUserLDAPHelper helper = new OSUserLDAPHelper();
                helper.getLDAPUser(creationUserName);                

                if (lUser != null) {
                    vUser = userAccessor.addUser(creationUserName, creationUserName, lUser.getEmail(), lUser.getFullName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vUser;
    }


    public void removeUsersByUsernameFromGroups(List userNames, List groupNames, ServiceContext context) throws RemoveException {
        log.debug("removeUsersByUsernamesFromGroupsByGroupname() called.");

        List usersNotFound = new ArrayList();
        // using map to get only unique groups. using treemap to keep groupnames in order
        Map groupsNotFoundMap = new TreeMap();

        for (Iterator itr = userNames.iterator(); itr.hasNext();) {
            String userid = (String) itr.next();

            if (userAccessor.getUser(userid)!=null) {
                for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                    String groupName = (String) iterator.next();
                    if (groupsNotFoundMap.get(groupName)==null && userAccessor.getGroup(groupName)!=null) {
                        userAccessor.removeMembership(groupName, userid);
                    }
                    else {
                        groupsNotFoundMap.put( "" + groupName, "");
                    }
                }
            }
            else {
                usersNotFound.add(userid);
            }
        }

        // If we failed, throw exception
        List groupsNotFound = new ArrayList(groupsNotFoundMap.keySet());
        if (usersNotFound.size()>0 || groupsNotFound.size()>0) {
            throw new RemoveException(getErrorMessage(usersNotFound, groupsNotFound, context));
        }
    }

    private String getErrorMessage(List usersNotFound, List groupsNotFound, ServiceContext context) {
        String msg = "";
        String concat = "";
        if (usersNotFound.size()>0) {
            msg += context.getText("error.usersNotFound") + ": " +
                    StringUtil.convertCollectionToCommaDelimitedString(usersNotFound) + ".";
            concat = " ";
        }

        if (groupsNotFound.size()>0) {
            msg += concat;
            msg += context.getText("error.groupsNotFound") + ": " +
                    StringUtil.convertCollectionToCommaDelimitedString(groupsNotFound) + ".";
        }

        return msg;
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
