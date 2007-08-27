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

package csum.confluence.permissionmgmt.service.impl;

import csum.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.service.exception.AddException;
import csum.confluence.permissionmgmt.service.exception.RemoveException;
import csum.confluence.permissionmgmt.service.exception.ServiceAuthenticationException;
import csum.confluence.permissionmgmt.service.vo.AdvancedUserQuerySubstringMatchType;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapService;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapServiceServiceLocator;
import csum.confluence.permissionmgmt.soap.jira.RemoteGroup;
import csum.confluence.permissionmgmt.soap.jira.RemoteUser;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.jira.JiraServiceAuthenticationContext;
import csum.confluence.permissionmgmt.util.jira.JiraSoapUtil;
import com.dolby.confluence.net.ldap.LDAPUser;
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

    private boolean matches(String value, String searchValue, String type) {
        log.debug("matches() called.");
        boolean result = false;
        if (value != null && searchValue != null && type != null) {
            if (type == AdvancedUserQuerySubstringMatchType.SUBSTRING_STARTS_WITH && value.startsWith(searchValue)) {
                result = true;
            } else
            if (type == AdvancedUserQuerySubstringMatchType.SUBSTRING_CONTAINS && value.indexOf(searchValue) != -1) {
                result = true;
            } else if (type == AdvancedUserQuerySubstringMatchType.SUBSTRING_ENDS_WITH && value.endsWith(searchValue)) {
                result = true;
            }
        }

        return result;
    }

    public void addUsersByUsernameToGroups(List userNames, List groupNames, ServiceContext context) throws AddException, ServiceAuthenticationException {
        log.debug("addUsersByUsernameToGroupsByGroupname() called. " +
                "usernames=" + StringUtil.convertCollectionToCommaDelimitedString(userNames) +
                ", groupnames=" + StringUtil.convertCollectionToCommaDelimitedString(groupNames));

        JiraServiceAuthenticationContext authContext = null;
        List usersNotFound = new ArrayList();
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
                RemoteUser currUser = jiraSoapService.getUser(token, userid);
                if (currUser == null) {
                    //create an user
                    //userid doesn't exists, if LDAP present then we will create User if it exists in LDAP.
                    if (isLDAPPresent) {
                        //create an user.

                        // TODO: the option to create users if they don't exist using LDAP info should be in config

                        // TODO: consider adding option and ability to create users if they don't exist, even if LDAP not used

                        currUser = createJiraUser(token, jiraSoapService, userid, isLDAPPresent);
                    }

                    //if user details not found in LDAP too, then retun userid in errorids
                    if (currUser == null) {

                        //for some reason we are unable to create user.
                        //add it to our notCreatedUser List.
                        usersNotFound.add(userid);

                        continue;

                    } else {
                        RemoteUser remoteUser = jiraSoapService.getUser(token, userid);
                        if (remoteUser != null) {

                            //Add this user to default group confluence-users
                            if (groupsNotFoundMap.get(ServiceConstants.CONFLUENCE_USERS_GROUP_NAME)==null) {
                                RemoteGroup remoteGroup = jiraSoapService.getGroup(token, ServiceConstants.CONFLUENCE_USERS_GROUP_NAME);
                                if (remoteGroup!=null) {
                                    jiraSoapService.addUserToGroup(token, remoteGroup, remoteUser);
                                }
                                else {
                                    groupsNotFoundMap.put(ServiceConstants.CONFLUENCE_USERS_GROUP_NAME, "");
                                }
                            }

                            if (groupsNotFoundMap.get(ServiceConstants.JIRA_USERS_GROUP_NAME)==null) {
                                RemoteGroup remoteGroup = jiraSoapService.getGroup(token, ServiceConstants.JIRA_USERS_GROUP_NAME);
                                if (remoteGroup!=null) {
                                    jiraSoapService.addUserToGroup(token, remoteGroup, remoteUser);
                                }
                                else {
                                    groupsNotFoundMap.put(ServiceConstants.JIRA_USERS_GROUP_NAME, "");
                                }
                            }
                        }
                        else {
                            usersNotFound.add(userid);
                        }
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

                            RemoteGroup remoteGroup = jiraSoapService.getGroup(token, lowercaseGroupName);
                            RemoteUser remoteUser = jiraSoapService.getUser(token, userid);

                            if (userAccessor.getGroup(lowercaseGroupName) != null && remoteGroup != null) {
                                userAccessor.addMembership(lowercaseGroupName, userid);
                                jiraSoapService.addUserToGroup(token, remoteGroup, remoteUser);
                            } else if (userAccessor.getGroup(groupName) != null) {
                                userAccessor.addMembership(groupName, userid);
                            } else {
                                groupsNotFoundMap.put("" + groupName, "");
                            }
                        }
                    }
                }
            }
        }
        catch (Throwable e) {
            log.error(e);
            throw new AddException(e.getMessage(), e);
        }
        finally {
            if (authContext != null) {
                try {
                    JiraSoapUtil.logout(authContext);
                }
                catch (Throwable t) {
                    log.error("Error in Jira logout", t);
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
                    vUser = jiraSoapService.createUser(token, creationUserName, creationUserName, lUser.getEmail(), lUser.getFullName());
                }
            }
        } catch (Throwable e) {
            log.error("Error creating Jira user", e);
        }

        return vUser;
    }

    public void removeUsersByUsernameFromGroups(List userNames, List groupNames, ServiceContext context) throws RemoveException, ServiceAuthenticationException {
        log.debug("removeUsersByUsernameFromGroups() called. " +
                "usernames=" + StringUtil.convertCollectionToCommaDelimitedString(userNames) +
                ", groupnames=" + StringUtil.convertCollectionToCommaDelimitedString(groupNames));
        JiraServiceAuthenticationContext authContext = null;

        List usersNotFound = new ArrayList();
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
                        if (groupsNotFoundMap.get(groupName) == null) {
                            RemoteGroup remoteGroup = jiraSoapService.getGroup(token, groupName);
                            if (remoteGroup != null) {
                                jiraSoapService.removeUserFromGroup(token, remoteGroup, remoteUser);
                            }
                        } else {
                            groupsNotFoundMap.put("" + groupName, "");
                        }
                    }
                } else {
                    usersNotFound.add(userid);
                }
            }
        }
        catch (Throwable e) {
            log.error(e);
            throw new RemoveException(e.getMessage(), e);
        }
        finally {
            if (authContext != null) {
                try {
                    JiraSoapUtil.logout(authContext);
                }
                catch (Throwable t) {
                    log.error("Error in Jira logout", t);
                }
            }
        }

        // If we failed, throw exception
        List groupsNotFound = new ArrayList(groupsNotFoundMap.keySet());
        if (usersNotFound.size() > 0 || groupsNotFound.size() > 0) {
            throw new RemoveException(getErrorMessage(usersNotFound, groupsNotFound, context));
        }
    }
}
