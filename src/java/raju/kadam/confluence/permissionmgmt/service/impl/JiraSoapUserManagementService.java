package raju.kadam.confluence.permissionmgmt.service.impl;

import raju.kadam.confluence.permissionmgmt.service.*;
import raju.kadam.confluence.permissionmgmt.service.vo.*;
import raju.kadam.confluence.permissionmgmt.soap.jira.JiraSoapService;
import raju.kadam.confluence.permissionmgmt.soap.jira.JiraSoapServiceServiceLocator;
import raju.kadam.confluence.permissionmgmt.soap.jira.RemoteGroup;
import raju.kadam.confluence.permissionmgmt.soap.jira.RemoteUser;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.util.JiraUtil;
import raju.kadam.confluence.permissionmgmt.util.UserUtil;

import java.util.List;
import java.util.ArrayList;

import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.DefaultPager;
import com.atlassian.user.search.page.PagerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 5, 2007
 * Time: 1:45:18 PM
 */
public class JiraSoapUserManagementService implements UserManagementService {

    private Log log = LogFactory.getLog(this.getClass());

    private CustomPermissionConfiguration customPermissionConfiguration;

    private boolean matches( String value, String searchValue, String type ) {
        log.debug("matches() called.");
        boolean result = false;
        if (value != null && searchValue != null && type != null) {
            if (type == AdvancedUserQuerySubstringMatchType.SUBSTRING_STARTS_WITH && value.startsWith(searchValue)) {
                result = true;
            }
            else if (type == AdvancedUserQuerySubstringMatchType.SUBSTRING_CONTAINS && value.indexOf(searchValue) != -1) {
                result = true;
            }
            else if (type == AdvancedUserQuerySubstringMatchType.SUBSTRING_ENDS_WITH && value.endsWith(searchValue)) {
                result = true;
            }
        }

        return result;
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

    protected List getAllJiraUsers(ServiceContext context) throws FindException {
        log.debug("getAllJiraUsers() called.");
        // TODO: refactor. converting from pager to list to pager is just plain stupid
        return PagerUtils.toList(findUsersForGroup("jira-users", context));
    }

    public AdvancedUserQueryResults findUsers(AdvancedUserQuery advancedUserQuery, ServiceContext context) throws FindException {
        log.debug("findUsers() called.");
        AdvancedUserQueryResults results = new AdvancedUserQueryResults();

        if (!advancedUserQuery.isDefined()) {
            // no need to do anything if no search defined
            return results;
        }

        List allJiraUsers = getAllJiraUsers(context);
        List users = new ArrayList();
        if (advancedUserQuery.isUsernameSearchDefined()) {
            for (int i=0; i<allJiraUsers.size(); i++) {
                DefaultUser jiraUser = (DefaultUser)allJiraUsers.get(0);
                if (matches(jiraUser.getName(), advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType())) {
                    users.add(jiraUser);
                }
            }
        }
        else if (advancedUserQuery.isFullnameSearchDefined()) {
            for (int i=0; i<allJiraUsers.size(); i++) {
                DefaultUser jiraUser = (DefaultUser)allJiraUsers.get(0);
                if (matches(jiraUser.getFullName(), advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType())) {
                    users.add(jiraUser);
                }
            }
        }
        else if (advancedUserQuery.isEmailSearchDefined()) {
            for (int i=0; i<allJiraUsers.size(); i++) {
                DefaultUser jiraUser = (DefaultUser)allJiraUsers.get(0);
                if (matches(jiraUser.getEmail(), advancedUserQuery.getPartialSearchTerm(), advancedUserQuery.getSubstringMatchType())) {
                    users.add(jiraUser);
                }
            }
        }

        Pager pager = new DefaultPager(users);
        results.setUsers(pager);

        return results;
    }

    public Pager findUsersForGroup(String groupName, ServiceContext context) throws FindException {
        log.debug("findUsersForGroup() called.");
        List users = new ArrayList();

        JiraSoapService jiraSoapService = null;
        String token = null;

        try
        {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());
            RemoteGroup remoteGroup = jiraSoapService.getGroup(token, groupName);
            if (remoteGroup != null) {
                RemoteUser[] jiraUsers = remoteGroup.getUsers();

                if (jiraUsers!=null)
                {
                    for (int i=0; i<jiraUsers.length; i++)
                    {
                        RemoteUser jiraUser = jiraUsers[i];
                        if (jiraUser != null)
                        {
                            DefaultUser user = new DefaultUser();
                            user.setEmail(jiraUser.getEmail());
                            user.setFullName(jiraUser.getFullname());
                            user.setName(jiraUser.getName());
                            users.add(user);
                        }
                    }
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (token != null)
            {
                try
                {
                    jiraSoapService.logout(token);
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }

        Pager pager = new DefaultPager(users);

        return pager;
    }

    public Pager findUsersWhoseNameStartsWith(String partialName, ServiceContext context) throws FindException {
        log.debug("findUsersWhoseNameStartsWith() called.");
        AdvancedUserQuery advancedUserQuery = new AdvancedUserQuery();
        advancedUserQuery.setLookupType(AdvancedUserQueryLookupType.USERNAME);
        advancedUserQuery.setPartialSearchTerm(partialName);
        advancedUserQuery.setSubstringMatchType(AdvancedUserQuerySubstringMatchType.SUBSTRING_STARTS_WITH);
        return findUsers(advancedUserQuery, context).getUsers();
    }

    public void addUsersByUsernameToGroups(List userNames, List groupNames, ServiceContext context) throws AddException
    {
        log.debug("addUsersByUsernameToGroup() called.");
        JiraSoapService jiraSoapService = null;
        String token = null;

        try
        {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());

            for (int i=0; i<groupNames.size(); i++) {
                String groupName = (String)groupNames.get(i);
                RemoteGroup remoteGroup = jiraSoapService.getGroup(token, groupName);

                for (int j=0; j<userNames.size(); j++) {
                    String userName = (String)userNames.get(j);
                    RemoteUser remoteUser = jiraSoapService.getUser(token, userName);

                    jiraSoapService.addUserToGroup(token, remoteGroup, remoteUser);
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (token != null)
            {
                try
                {
                    jiraSoapService.logout(token);
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }
    }

    public void removeUsersByUsernameFromGroups(List userNames, List groupNames, ServiceContext context) throws RemoveException
    {
        log.debug("removeUsersByUsernameFromGroup() called.");
        JiraSoapService jiraSoapService = null;
        String token = null;

        try
        {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());

            for (int i=0; i<groupNames.size(); i++) {
                String groupName = (String)groupNames.get(i);
                RemoteGroup remoteGroup = jiraSoapService.getGroup(token, groupName);

                for (int j=0; j<userNames.size(); j++) {
                    String userName = (String)userNames.get(j);
                    RemoteUser remoteUser = jiraSoapService.getUser(token, userName);

                    jiraSoapService.removeUserFromGroup(token, remoteGroup, remoteUser);
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (token != null)
            {
                try
                {
                    jiraSoapService.logout(token);
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }
    }

    public boolean isMemberOf(String userName, String groupName) throws FindException
    {
        log.debug("isMemberOf() called.");
        boolean result = false;

        JiraSoapService jiraSoapService = null;
        String token = null;

        try
        {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());
            RemoteGroup remoteGroup = jiraSoapService.getGroup(token, groupName);
            if (remoteGroup != null) {
                RemoteUser[] users = remoteGroup.getUsers();

                if (users!=null)
                {
                    for (int i=0; i<users.length; i++)
                    {
                        RemoteUser thisUser = users[i];
                        if (thisUser != null && userName.equals(thisUser.getName()))
                        {
                            result = true;
                        }
                    }
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (token != null)
            {
                try
                {
                    jiraSoapService.logout(token);
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }

        return result;
    }

    public CustomPermissionConfiguration getCustomPermissionConfiguration() {
        return customPermissionConfiguration;
    }

    public void setCustomPermissionConfiguration(CustomPermissionConfiguration customPermissionConfiguration) {
        this.customPermissionConfiguration = customPermissionConfiguration;
    }
}
