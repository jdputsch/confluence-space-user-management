/**
 * Copyright (c) 2007-2009, Custom Space User Management Plugin Development Team
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

package csum.confluence.permissionmgmt.service.impl;

import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.dolby.confluence.net.ldap.LDAPUser;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.service.exception.AddException;
import csum.confluence.permissionmgmt.service.exception.RemoveException;
import csum.confluence.permissionmgmt.service.exception.UsersNotFoundException;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.logging.LogUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class ConfluenceUserManagementService extends BaseUserManagementService {

    public void addUsersByUsernameToGroups(List userNames, List groupNames, ServiceContext context) throws UsersNotFoundException, AddException {
        log.debug("addUsersByUsernameToGroupsByGroupname() called. " +
                "usernames=" + StringUtil.convertCollectionToCommaDelimitedString(userNames) +
                ", groupnames=" + StringUtil.convertCollectionToCommaDelimitedString(groupNames));
        CustomPermissionConfiguration config = getCustomPermissionConfiguration();

        List usersNotFound = new ArrayList();
        Map userIdToGroupNameMapForMembershipAdditionProblems = new TreeMap();
        // using map to get only unique groups. using treemap to keep groupnames in order
        Map groupsNotFoundMap = new TreeMap();

        boolean isLDAPPresent = config.getLdapAuthUsed().equals(CustomPermissionConfigConstants.YES) ? true : false;

        //Associate selected user-groups to all users.
        for (Iterator itr = userNames.iterator(); itr.hasNext();) {
            //First check if given user is present or not
            String userid = (String) itr.next();
            User user = userAccessor.getUser(userid);
            if (user == null) {
                //create an user
                //userid doesn't exists, if LDAP present then we will create User if it exists in LDAP.
                if (isLDAPPresent) {
                    //create an user.

                    // TODO: the option to create users if they don't exist using LDAP info should be in config

                    // TODO: consider adding option and ability to create users if they don't exist, even if LDAP not used

                    user = createConfUser(userid, isLDAPPresent);
                }

                //if user details not found in LDAP too, then retun userid in errorids
                if (user == null) {

                    //for some reason we are unable to create user.
                    //add it to our notCreatedUser List.
                    usersNotFound.add(userid);

                    continue;

                } else {
                    //Add this user to default group confluence-users
                    addMembershipPassivelyAndTrackingErrors(ServiceConstants.CONFLUENCE_USERS_GROUP_NAME, user, groupsNotFoundMap, usersNotFound, userIdToGroupNameMapForMembershipAdditionProblems);
                }
            }

            //If user exists then associate him/her to all specified groups
            if (user != null) {
                //Associate this user to all selected user-groups
                for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                    String groupName = (String) iterator.next();
                    addMembershipPassivelyAndTrackingErrors(groupName, user, groupsNotFoundMap, usersNotFound, userIdToGroupNameMapForMembershipAdditionProblems);
                }
            }
        }

        // If we failed, throw exception
        List groupsNotFound = new ArrayList(groupsNotFoundMap.keySet());
        if (usersNotFound.size() > 0 && groupsNotFound.size() == 0 && userIdToGroupNameMapForMembershipAdditionProblems.size() == 0) {
            // a less critical error that we don't want to log the same way
            throw new UsersNotFoundException(context.getText("csum.manager.error.usersnotfound") + ": " +
                    StringUtil.convertCollectionToCommaDelimitedString(usersNotFound) + ".");
        } else if (groupsNotFound.size() > 0 || userIdToGroupNameMapForMembershipAdditionProblems.size() > 0) {
            throw new AddException(getRemoveUsersByUsernameFromGroupsErrorMessage(usersNotFound, groupsNotFound, userIdToGroupNameMapForMembershipAdditionProblems, context));
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
                lUser = getLDAPUser(creationUserName);

                if (lUser != null) {
                    vUser = userAccessor.addUser(creationUserName, creationUserName, lUser.getEmail(), lUser.getFullName());

                    if (vUser == null) {
                        LogUtil.warnWithRemoteUserInfo(log, "userAccessor.addUser(...) returned null for userid '" + creationUserName + ". User addition may have been unsuccessful.");
                    }
                }
                else {
                    LogUtil.warnWithRemoteUserInfo(log, "No LDAP user found for userid '" + creationUserName + "'. Unable to add user.");
                }
            }
        } catch (Exception e) {
            LogUtil.errorWithRemoteUserInfo(log, "Error creating confluence user " + creationUserName, e);
        }

        return vUser;
    }

    private void addMembershipPassivelyAndTrackingErrors(String groupName, User user, Map groupsNotFoundMap, List usersNotFound, Map userIdToGroupNameMapForMembershipAdditionProblems) {
        if (groupsNotFoundMap.get(groupName) == null && user != null) {
            try {
                Group group = userAccessor.getGroup(groupName);
                if (group == null) {

                    String lowercaseGroupName = groupName.toLowerCase();
                    if (lowercaseGroupName.equals(groupName)) {
                        // group name was already lowercase
                        LogUtil.warnWithRemoteUserInfo(log, "Failed adding " + user.getName() + " to " + groupName + ". Group didn't exist");
                        groupsNotFoundMap.put("" + groupName, "");
                        return;
                    } else {
                        // There is a bug where groupname must be lowercase but we handle both
                        // scenarios just in case it gets fixed: http://jira.atlassian.com/browse/CONF-9224

                        log.debug("No group exists for groupname " + groupName + " so will try lowercase groupName");
                        group = userAccessor.getGroup(lowercaseGroupName);
                        if (group == null) {
                            LogUtil.warnWithRemoteUserInfo(log, "Failed adding " + user.getName() + " to " + groupName + ". Group didn't exist (tried regular case and lowercase groupname)");
                            // make sure to use regular case here since it is checked against in calling method
                            groupsNotFoundMap.put("" + groupName, "");
                        }
                    }
                }

                if (group != null) {
                    if (userAccessor.isReadOnly(group)) {
                        log.debug("Not adding '" + user.getName() + "' to group '" + groupName + "' because group was read-only");
                        userIdToGroupNameMapForMembershipAdditionProblems.put("" + user.getName(), "" + groupName);
                    }
                    else {
                        if (!isMemberOf(user.getName(), groupName)) {
                            log.debug("Adding " + user.getName() + " to group " + groupName);
                            userAccessor.addMembership(group, user);
                        }
                        else {
                            log.debug("User " + user.getName() + " was already a member of group " + groupName);
                        }
                    }
                }
            }
            catch (Throwable t) {
                LogUtil.errorWithRemoteUserInfo(log, "Failed adding " + user.getName() + " to " + groupName);
                // using "" + to guard against nulls
                userIdToGroupNameMapForMembershipAdditionProblems.put("" + user.getName(), "" + groupName);
            }
        }
    }

    public void removeUsersByUsernameFromGroups(List userNames, List groupNames, ServiceContext context) throws UsersNotFoundException, RemoveException {
        log.debug("removeUsersByUsernameFromGroups() called. " +
                "usernames=" + StringUtil.convertCollectionToCommaDelimitedString(userNames) +
                ", groupnames=" + StringUtil.convertCollectionToCommaDelimitedString(groupNames));

        List usersNotFound = new ArrayList();
        Map userIdToGroupNameMapForMembershipRemovalProblems = new TreeMap();
        // using map to get only unique groups. using treemap to keep groupnames in order
        Map groupsNotFoundMap = new TreeMap();

        for (Iterator itr = userNames.iterator(); itr.hasNext();) {
            String userid = (String) itr.next();
            User user = userAccessor.getUser(userid);
            if (user != null) {
                for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                    String groupName = (String) iterator.next();
                    removeMembershipPassivelyAndTrackingErrors(groupName, user, groupsNotFoundMap, usersNotFound, userIdToGroupNameMapForMembershipRemovalProblems);
                }
            } else {
                usersNotFound.add(userid);
            }
        }

        // If we failed, throw exception
        List groupsNotFound = new ArrayList(groupsNotFoundMap.keySet());
        if (usersNotFound.size() > 0 && groupsNotFound.size() == 0 && userIdToGroupNameMapForMembershipRemovalProblems.size() == 0) {
            // a less critical error that we don't want to log the same way
            throw new UsersNotFoundException(context.getText("csum.manager.error.usersnotfound") + ": " +
                    StringUtil.convertCollectionToCommaDelimitedString(usersNotFound) + ".");
        } else if (groupsNotFound.size() > 0 || userIdToGroupNameMapForMembershipRemovalProblems.size() > 0) {
            throw new RemoveException(getRemoveUsersByUsernameFromGroupsErrorMessage(usersNotFound, groupsNotFound, userIdToGroupNameMapForMembershipRemovalProblems, context));
        }
    }

    private void removeMembershipPassivelyAndTrackingErrors(String groupName, User user, Map groupsNotFoundMap, List usersNotFound, Map userIdToGroupNameMapForMembershipRemovalProblems) {
        if (groupsNotFoundMap.get(groupName) == null && user != null) {
            try {
                Group group = userAccessor.getGroup(groupName);
                if (group == null) {

                    String lowercaseGroupName = groupName.toLowerCase();
                    if (lowercaseGroupName.equals(groupName)) {
                        // group name was already lowercase
                        LogUtil.warnWithRemoteUserInfo(log, "Failed adding " + user.getName() + " to " + groupName + ". Group didn't exist");
                        groupsNotFoundMap.put("" + groupName, "");
                        return;
                    } else {
                        // There is a bug where groupname must be lowercase but we handle both
                        // scenarios just in case it gets fixed: http://jira.atlassian.com/browse/CONF-9224

                        log.debug("No group exists for groupname " + groupName + " so will try lowercase groupName");
                        group = userAccessor.getGroup(lowercaseGroupName);
                        if (group == null) {
                            LogUtil.warnWithRemoteUserInfo(log, "Failed adding " + user.getName() + " to " + groupName + ". Group didn't exist (tried regular case and lowercase groupname)");
                            // make sure to use regular case here since it is checked against in calling method
                            groupsNotFoundMap.put("" + groupName, "");
                        }
                    }
                }

                if (group != null) {
                    log.debug("Removing " + user.getName() + " from " + groupName);
                    if (userAccessor.isReadOnly(group)) {
                        log.debug("Not removing '" + user.getName() + "' from group '" + groupName + "' because group was read-only");
                        userIdToGroupNameMapForMembershipRemovalProblems.put("" + user.getName(), "" + groupName);
                    }
                    else {
                        if (isMemberOf(user.getName(), groupName)) {
                            log.debug("Removing " + user.getName() + " from group " + groupName);
                            userAccessor.removeMembership(group, user);
                        }
                        else {
                            log.debug("User " + user.getName() + " was not a member of group " + groupName + " so did not have to remove.");
                        }
                    }
                }
            }
            catch (Throwable t) {
                LogUtil.errorWithRemoteUserInfo(log, "Failed removing " + user.getName() + " from " + groupName);
                // using "" + to guard against nulls
                userIdToGroupNameMapForMembershipRemovalProblems.put("" + user.getName(), "" + groupName);
            }
        }
    }
}
