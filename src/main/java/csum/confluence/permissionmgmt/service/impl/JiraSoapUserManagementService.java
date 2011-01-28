/**
 * Copyright (c) 2007-2010, Custom Space User Management Plugin Development Team
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

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.user.GroupManager;
import com.atlassian.user.UserManager;
import com.dolby.confluence.net.ldap.LDAPUser;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.service.exception.AddException;
import csum.confluence.permissionmgmt.service.exception.RemoveException;
import csum.confluence.permissionmgmt.service.exception.ServiceAuthenticationException;
import csum.confluence.permissionmgmt.service.exception.UsersNotFoundException;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapService;
import csum.confluence.permissionmgmt.soap.jira.RemoteGroup;
import csum.confluence.permissionmgmt.soap.jira.RemoteUser;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.jira.JiraServiceAuthenticationContext;
import csum.confluence.permissionmgmt.util.jira.JiraSoapUtil;
import csum.confluence.permissionmgmt.util.logging.LogUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class JiraSoapUserManagementService extends BaseUserManagementService {

    private Log log = LogFactory.getLog(this.getClass());

    // autowired by constructor injection via Atlassian Plugin framework/OSGi.
    public JiraSoapUserManagementService(UserAccessor userAccessor,
                                         UserManager userManager,
                                         GroupManager groupManager,
                                         CustomPermissionConfiguration customPermissionConfiguration) {
        super(userAccessor,
                userManager,
                groupManager,
                customPermissionConfiguration);
    }

    public void addUsersByUsernameToGroups(List userNames, List groupNames, ServiceContext context) throws UsersNotFoundException, AddException, ServiceAuthenticationException {
        log.debug("addUsersByUsernameToGroupsByGroupname() called. " +
                "usernames=" + StringUtil.convertCollectionToCommaDelimitedString(userNames) +
                ", groupnames=" + StringUtil.convertCollectionToCommaDelimitedString(groupNames));

        JiraServiceAuthenticationContext authContext = null;
        List usersNotFound = new ArrayList();
        Map userIdToGroupNameMapForMembershipAdditionProblems = new TreeMap();
        // using map to get only unique groups. using treemap to keep groupnames in order
        Map groupsNotFoundMap = new TreeMap();

        try {
            authContext = JiraSoapUtil.login(context);
            JiraSoapService jiraSoapService = authContext.getJiraSoapService();
            String token = authContext.getToken();

            CustomPermissionConfiguration config = getCustomPermissionConfiguration();

            boolean isLDAPPresent = config.getLdapAuthUsed().equals(CustomPermissionConfigConstants.YES) ? true : false;

            //Associate selected user-groups to all users.
            for (Iterator itr = userNames.iterator(); itr.hasNext();) {
                //First check if given user is present or not
                String userid = (String) itr.next();

                if (!usersNotFound.contains(userid)) {
                    RemoteUser remoteUser = jiraSoapService.getUser(token, userid);
                    if (remoteUser == null) {
                        //userid doesn't exist, but if LDAP present then we will create user if it exists in LDAP.
                        if (isLDAPPresent) {
                            System.out.println("Getting user from LDAP ...");
                            remoteUser = createJiraUser(token, jiraSoapService, userid, isLDAPPresent);
                        }

                        //if user details not found in LDAP too, then return userid in errorids
                        if (remoteUser == null) {

                            //for some reason we are unable to create user.
                            //add it to our notCreatedUser List.
                            usersNotFound.add(userid);

                            continue;

                        } else {
                            addMembershipToJiraPassivelyAndTrackingErrors(jiraSoapService, token, ServiceConstants.CONFLUENCE_USERS_GROUP_NAME, remoteUser, groupsNotFoundMap, usersNotFound, userIdToGroupNameMapForMembershipAdditionProblems);
                            addMembershipToJiraPassivelyAndTrackingErrors(jiraSoapService, token, ServiceConstants.JIRA_USERS_GROUP_NAME, remoteUser, groupsNotFoundMap, usersNotFound, userIdToGroupNameMapForMembershipAdditionProblems);
                        }
                    }

                    //If user exists then associate him/her to all selected User
                    if (remoteUser != null) {
                        //Associate this user to all selected user-groups
                        for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                            String groupName = (String) iterator.next();
                            addMembershipToJiraPassivelyAndTrackingErrors(jiraSoapService, token, groupName, remoteUser, groupsNotFoundMap, usersNotFound, userIdToGroupNameMapForMembershipAdditionProblems);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LogUtil.errorWithRemoteUserInfo(log, "Failed adding users to groups!", e);
            throw new AddException(e.getMessage(), e);
        } finally {
            if (authContext != null) {
                try {
                    JiraSoapUtil.logout(authContext);
                } catch (Throwable t) {
                    LogUtil.errorWithRemoteUserInfo(log, "Error in Jira logout", t);
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
    private RemoteUser createJiraUser(String token, JiraSoapService jiraSoapService, String creationUserName, boolean isLDAPAvailable) {
        log.debug("createConfUser() called. creationUserName=" + creationUserName + " isLDAPAvailable" + isLDAPAvailable);
        RemoteUser vUser = null;
        LDAPUser lUser = null;

        try {
            //if LDAP Lookup is available, get information from there.
            if (isLDAPAvailable) {
                //log.debug("LDAP Lookup available");
                //Get user details from LDAP.
                lUser = getLDAPUser(creationUserName);

                if (lUser != null) {
                    //createUser(String token, String username, String password, String fullName, String email)
                    vUser = jiraSoapService.createUser(token, creationUserName, creationUserName, lUser.getFullName(), lUser.getEmail());

                    if (vUser == null) {
                        LogUtil.warnWithRemoteUserInfo(log, "jiraSoapService.createUser(...) returned null for userid '" + creationUserName + ". User addition may have been unsuccessful.");
                    }
                } else {
                    LogUtil.warnWithRemoteUserInfo(log, "No LDAP user found for userid '" + creationUserName + "'. Unable to add user.");
                }
            }
        } catch (Throwable e) {
            LogUtil.errorWithRemoteUserInfo(log, "Error creating Jira user", e);
        }

        return vUser;
    }

    private void addMembershipToJiraPassivelyAndTrackingErrors(JiraSoapService jiraSoapService, String token, String groupName, RemoteUser remoteUser, Map groupsNotFoundMap, List usersNotFound, Map userIdToGroupNameMapForMembershipAdditionProblems) {
        if (groupsNotFoundMap.get(groupName) == null && remoteUser != null) {
            try {
                RemoteGroup remoteGroup = jiraSoapService.getGroup(token, groupName);
                if (remoteGroup == null) {

                    String lowercaseGroupName = groupName.toLowerCase();
                    if (lowercaseGroupName.equals(groupName)) {
                        // group name was already lowercase
                        LogUtil.warnWithRemoteUserInfo(log, "Failed adding " + remoteUser.getName() + " to " + groupName + " in Jira. Jira group didn't exist");
                        groupsNotFoundMap.put("" + groupName, "");
                        return;
                    } else {
                        // There is a bug where groupname must be lowercase but we handle both
                        // scenarios just in case it gets fixed: http://jira.atlassian.com/browse/CONF-9224

                        log.debug("No Jira group exists for groupname " + groupName + " so will try lowercase groupName");
                        remoteGroup = jiraSoapService.getGroup(token, lowercaseGroupName);
                        if (remoteGroup == null) {
                            LogUtil.warnWithRemoteUserInfo(log, "Failed adding " + remoteUser.getName() + " to " + groupName + " in Jira. Jira group didn't exist (tried regular case and lowercase groupname)");
                            // make sure to use regular case here since it is checked against in calling method
                            groupsNotFoundMap.put("" + groupName, "");
                        }
                    }
                }

                if (remoteGroup != null) {
                    if (!isMemberOf(remoteUser.getName(), groupName)) {
                        if ("jira-users".equals(groupName)) {
                            log.debug("We won't add " + remoteUser.getName() + " to group jira-users (SUSR-102).");
                        } else {
                            log.debug("Adding " + remoteUser.getName() + " to Jira group " + groupName);
                            jiraSoapService.addUserToGroup(token, remoteGroup, remoteUser);
                        }
                    } else {
                        log.debug("Did not add user " + remoteUser.getName() + " to group " + groupName + ". It was already a member.");
                    }
                }
            } catch (Throwable t) {
                LogUtil.errorWithRemoteUserInfo(log, "Failed adding " + remoteUser.getName() + " to " + groupName, t);
                // using "" + to guard against nulls
                userIdToGroupNameMapForMembershipAdditionProblems.put("" + remoteUser.getName(), "" + groupName);
            }
        }
    }

    public void removeUsersByUsernameFromGroups(List userNames, List groupNames, ServiceContext context) throws UsersNotFoundException, RemoveException, ServiceAuthenticationException {
        log.debug("removeUsersByUsernameFromGroups() called. " +
                "usernames=" + StringUtil.convertCollectionToCommaDelimitedString(userNames) +
                ", groupnames=" + StringUtil.convertCollectionToCommaDelimitedString(groupNames));
        JiraServiceAuthenticationContext authContext = null;

        List usersNotFound = new ArrayList();
        Map userIdToGroupNameMapForMembershipRemovalProblems = new TreeMap();
        // using map to get only unique groups. using treemap to keep groupnames in order
        Map groupsNotFoundMap = new TreeMap();

        try {
            authContext = JiraSoapUtil.login(context);
            JiraSoapService jiraSoapService = authContext.getJiraSoapService();
            String token = authContext.getToken();

            for (Iterator itr = userNames.iterator(); itr.hasNext();) {
                String userid = (String) itr.next();

                RemoteUser remoteUser = authContext.getJiraSoapService().getUser(token, userid);
                if (remoteUser != null) {
                    for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
                        String groupName = (String) iterator.next();
                        removeMembershipFromJiraPassivelyAndTrackingErrors(jiraSoapService, token, groupName, remoteUser, groupsNotFoundMap, usersNotFound, userIdToGroupNameMapForMembershipRemovalProblems);
                    }
                } else {
                    usersNotFound.add(userid);
                }
            }
        } catch (Throwable e) {
            LogUtil.errorWithRemoteUserInfo(log, "Failed removing users from groups!", e);
            throw new RemoveException(e.getMessage(), e);
        } finally {
            if (authContext != null) {
                try {
                    JiraSoapUtil.logout(authContext);
                } catch (Throwable t) {
                    LogUtil.errorWithRemoteUserInfo(log, "Error in Jira logout", t);
                }
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

    private void removeMembershipFromJiraPassivelyAndTrackingErrors(JiraSoapService jiraSoapService, String token, String groupName, RemoteUser remoteUser, Map groupsNotFoundMap, List usersNotFound, Map userIdToGroupNameMapForMembershipRemovalProblems) {
        if (groupsNotFoundMap.get(groupName) == null && remoteUser != null) {
            try {
                RemoteGroup remoteGroup = jiraSoapService.getGroup(token, groupName);
                if (remoteGroup == null) {

                    String lowercaseGroupName = groupName.toLowerCase();
                    if (lowercaseGroupName.equals(groupName)) {
                        // group name was already lowercase
                        LogUtil.warnWithRemoteUserInfo(log, "Failed adding " + remoteUser.getName() + " to " + groupName + " in Jira. Jira group didn't exist");
                        groupsNotFoundMap.put("" + groupName, "");
                        return;
                    } else {
                        // There is a bug where groupname must be lowercase but we handle both
                        // scenarios just in case it gets fixed: http://jira.atlassian.com/browse/CONF-9224

                        log.debug("No Jira group exists for groupname " + groupName + " so will try lowercase groupName");
                        remoteGroup = jiraSoapService.getGroup(token, lowercaseGroupName);
                        if (remoteGroup == null) {
                            LogUtil.warnWithRemoteUserInfo(log, "Failed adding " + remoteUser.getName() + " to " + groupName + " in Jira. Jira group didn't exist (tried regular case and lowercase groupname)");
                            // make sure to use regular case here since it is checked against in calling method
                            groupsNotFoundMap.put("" + groupName, "");
                        }
                    }
                }

                if (remoteGroup != null) {
                    if (isMemberOf(remoteUser.getName(), groupName)) {
                        if ("jira-users".equals(groupName)) {
                            log.debug("We will not remove " + remoteUser.getName() + " from group jira-users (SUSR-102).");
                        } else {
                            log.debug("Removing " + remoteUser.getName() + " from Jira group " + groupName);
                            jiraSoapService.removeUserFromGroup(token, remoteGroup, remoteUser);
                        }
                    } else {
                        log.debug("Did not remove user " + remoteUser.getName() + " from group " + groupName + ", because it was not a member of that group.");
                    }

                }
            } catch (Throwable t) {
                LogUtil.errorWithRemoteUserInfo(log, "Failed removing " + remoteUser.getName() + " from Jira group " + groupName, t);
                // using "" + to guard against nulls
                userIdToGroupNameMapForMembershipRemovalProblems.put("" + remoteUser.getName(), "" + groupName);
            }
        }
    }
}
