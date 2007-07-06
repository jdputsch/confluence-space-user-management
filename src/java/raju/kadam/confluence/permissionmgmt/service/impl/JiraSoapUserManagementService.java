package raju.kadam.confluence.permissionmgmt.service.impl;

import raju.kadam.confluence.permissionmgmt.service.*;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedQueryType;
import raju.kadam.confluence.permissionmgmt.soap.jira.JiraSoapService;
import raju.kadam.confluence.permissionmgmt.soap.jira.JiraSoapServiceServiceLocator;
import raju.kadam.confluence.permissionmgmt.soap.jira.RemoteGroup;
import raju.kadam.confluence.permissionmgmt.soap.jira.RemoteUser;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.util.JiraUtil;
import raju.kadam.confluence.permissionmgmt.util.UserUtil;

import java.util.List;
import java.util.ArrayList;

import com.atlassian.user.User;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.search.query.UserNameTermQuery;
import com.atlassian.user.search.query.FullNameTermQuery;
import com.atlassian.user.search.query.EmailTermQuery;
import com.atlassian.user.search.query.GroupNameTermQuery;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.impl.osuser.OSUUser;
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
        boolean result = false;
        if (value != null && searchValue != null && type != null) {
            if (type == AdvancedQueryType.SUBSTRING_STARTS_WITH && value.startsWith(searchValue)) {
                result = true;
            }
            else if (type == AdvancedQueryType.SUBSTRING_CONTAINS && value.indexOf(searchValue) != -1) {
                result = true;
            }
            else if (type == AdvancedQueryType.SUBSTRING_ENDS_WITH && value.endsWith(searchValue)) {
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
        return findUsersForGroup("jira-users", context);
    }

    public AdvancedUserQueryResults findUsers(AdvancedUserQuery advancedUserQuery, ServiceContext context) throws FindException {

        AdvancedUserQueryResults results = new AdvancedUserQueryResults();

        if (!advancedUserQuery.isDefined()) {
            // no need to do anything if no search defined
            return results;
        }

        List users = getAllJiraUsers(context);

        boolean ranQueryAtLeastOnce = false;
        if (advancedUserQuery.isUsernameSearchDefined()) {
            ArrayList returnedUsers = new ArrayList();
            for (int i=0; i<users.size(); i++) {
                DefaultUser user = (DefaultUser)users.get(0);
                if (matches(user.getName(), advancedUserQuery.getPartialUserName(), advancedUserQuery.getUserNameSearchType())) {
                    returnedUsers.add(user);
                }
            }
            users = findIntersection(users, returnedUsers, ranQueryAtLeastOnce);
            ranQueryAtLeastOnce = true;
        }

        if (advancedUserQuery.isFullnameSearchDefined()) {
            ArrayList returnedUsers = new ArrayList();
            for (int i=0; i<users.size(); i++) {
                DefaultUser user = (DefaultUser)users.get(0);
                if (matches(user.getName(), advancedUserQuery.getPartialFullName(), advancedUserQuery.getFullNameSearchType())) {
                    returnedUsers.add(user);
                }
            }
            users = findIntersection(users, returnedUsers, ranQueryAtLeastOnce);
            ranQueryAtLeastOnce = true;
        }

        if (advancedUserQuery.isEmailSearchDefined()) {
            ArrayList returnedUsers = new ArrayList();
            for (int i=0; i<users.size(); i++) {
                DefaultUser user = (DefaultUser)users.get(0);
                if (matches(user.getName(), advancedUserQuery.getPartialEmail(), advancedUserQuery.getEmailSearchType())) {
                    returnedUsers.add(user);
                }
            }
            users = findIntersection(users, returnedUsers, ranQueryAtLeastOnce);
            ranQueryAtLeastOnce = true;
        }

        //TODO: this needs to be disabled in the UI but with fields still populated with valid values as hidden post param
        if (advancedUserQuery.isGroupnameSearchDefined()) {
            results.setGroupNameFieldMessage("This feature is currently unsupported for systems using Jira for user and group management.");
        }

        results.setUsers(users);

        return results;
    }

    public List findUsersForGroup(String groupName, ServiceContext context) throws FindException {
        List results = new ArrayList();

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
                        if (thisUser != null)
                        {
                            DefaultUser user = new DefaultUser();
                            user.setEmail(thisUser.getEmail());
                            user.setFullName(thisUser.getFullname());
                            user.setName(thisUser.getName());
                            results.add(user);
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

        return results;
    }

    public List findUsersWhoseNameStartsWith(String partialName, ServiceContext context) throws FindException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addUsersByUsernameToGroup(List userNames, String groupName, ServiceContext context) throws AddException
    {
        JiraSoapService jiraSoapService = null;
        String token = null;

        try
        {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());
            RemoteGroup remoteGroup = jiraSoapService.getGroup(token, groupName);

            for (int j=0; j<userNames.size(); j++) {
                String userName = (String)userNames.get(j);
                RemoteUser remoteUser = jiraSoapService.getUser(token, userName);

                jiraSoapService.addUserToGroup(token, remoteGroup, remoteUser);
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

    public void removeUsersByUsernameFromGroup(List userNames, String groupName, ServiceContext context) throws RemoveException
    {
        JiraSoapService jiraSoapService = null;
        String token = null;

        try
        {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(JiraUtil.getJiraSoapUrl());
            jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            token = jiraSoapService.login(JiraUtil.getJiraSoapUsername(), JiraUtil.getJiraSoapPassword());
            RemoteGroup remoteGroup = jiraSoapService.getGroup(token, groupName);

            for (int j=0; j<userNames.size(); j++) {
                String userName = (String)userNames.get(j);
                RemoteUser remoteUser = jiraSoapService.getUser(token, userName);

                jiraSoapService.removeUserFromGroup(token, remoteGroup, remoteUser);
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
