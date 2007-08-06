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

import com.atlassian.user.User;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.service.exception.AddException;
import raju.kadam.confluence.permissionmgmt.service.exception.RemoveException;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.util.StringUtil;
import raju.kadam.confluence.permissionmgmt.util.ldap.LDAPUser;
import raju.kadam.confluence.permissionmgmt.util.ldap.OSUserLDAPHelper;

import java.util.*;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class ConfluenceUserManagementService extends BaseUserManagementService {

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

                    currUser = createConfUser(userid, isLDAPPresent);
                }

                //if user details not found in LDAP too, then retun userid in errorids
                if (currUser == null) {

                    //for some reason we are unable to create user.
                    //add it to our notCreatedUser List.
                    usersNotFound.add(userid);

                    continue;

                } else {
                    //Add this user to default group confluence-users
                    userAccessor.addMembership(ServiceConstants.CONFLUENCE_USERS_GROUP_NAME, userid);
                }
            }

            //If user exists then associate him/her to all selected usergroups
            if (currUser != null) {
                //Associate this user to all selected user-groups
                for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                    String groupName = (String) iterator.next();
                    if (groupsNotFoundMap.get(groupName) == null) {
                        // Am thoroughly confounded that the groupname here has to be lowercase. have backup check for
                        // regular (mixed-case) lookup, just in case that is a bug.
                        // TODO: test this and submit bug as needed
                        String lowercaseGroupName = groupName.toLowerCase();
                        if (userAccessor.getGroup(lowercaseGroupName) != null) {
                            userAccessor.addMembership(lowercaseGroupName, userid);
                        } else if (userAccessor.getGroup(groupName) != null) {
                            userAccessor.addMembership(groupName, userid);
                        } else {
                            groupsNotFoundMap.put("" + groupName, "");
                        }
                    }
                }
            }
        }

        // If we failed, throw exception
        List groupsNotFound = new ArrayList(groupsNotFoundMap.keySet());
        if (usersNotFound.size() > 0 || groupsNotFound.size() > 0) {
            throw new AddException(getErrorMessage(usersNotFound, groupsNotFound, context));
        }
    }

    //This method will be used to create an user when Confluence is used for Managing Wiki Users
    private User createConfUser(String creationUserName, boolean isLDAPAvailable) {
        log.debug("createConfUser() called. creationUserName=" + creationUserName + " isLDAPAvailable" + isLDAPAvailable);
        User vUser = null;
        LDAPUser lUser = null;

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
        log.debug("removeUsersByUsernameFromGroups() called. " +
                "usernames=" + StringUtil.convertCollectionToCommaDelimitedString(userNames) +
                ", groupnames=" + StringUtil.convertCollectionToCommaDelimitedString(groupNames));

        List usersNotFound = new ArrayList();
        // using map to get only unique groups. using treemap to keep groupnames in order
        Map groupsNotFoundMap = new TreeMap();

        for (Iterator itr = userNames.iterator(); itr.hasNext();) {
            String userid = (String) itr.next();

            if (userAccessor.getUser(userid) != null) {
                for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                    String groupName = (String) iterator.next();
                    if (groupsNotFoundMap.get(groupName) == null && userAccessor.getGroup(groupName) != null) {
                        userAccessor.removeMembership(groupName, userid);
                    } else {
                        groupsNotFoundMap.put("" + groupName, "");
                    }
                }
            } else {
                usersNotFound.add(userid);
            }
        }

        // If we failed, throw exception
        List groupsNotFound = new ArrayList(groupsNotFoundMap.keySet());
        if (usersNotFound.size() > 0 || groupsNotFound.size() > 0) {
            throw new RemoveException(getErrorMessage(usersNotFound, groupsNotFound, context));
        }
    }
}
