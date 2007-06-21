package raju.kadam.confluence.permissionmgmt.service.impl;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.user.search.query.*;
import raju.kadam.confluence.permissionmgmt.service.UserManagementService;
import raju.kadam.confluence.permissionmgmt.util.UserUtil;
import raju.kadam.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:48:36 AM
 */
public class SpaceConfUserManagementService implements UserManagementService {

    private UserAccessor userAccessor;

    public SpaceConfUserManagementService() {
        super();

        // autowiring will set the components on this class as needed
        com.atlassian.spring.container.ContainerManager.autowireComponent(this);
    }

    public List findUsersForGroup(String groupName) {
        Group group = userAccessor.getGroup(groupName);
        return findUsersForGroup(group);
    }

    public List findUsersForGroup(Group group) {

        //TODO: too many queries. will be unacceptably slow with lots of users and needs workaround.
        List results = new ArrayList();
        Pager pager = userAccessor.getMemberNames(group);
        List memberNames = PagerUtils.toList(pager);
        for (int i=0;i<memberNames.size();i++) {
            String username = (String)memberNames.get(i);
            User user = userAccessor.getUser(username);
            results.add(user);
        }

        return results;
    }

    public List findUsersWhoseNameStartsWith(String partialName) {

        List users = new ArrayList();

        try {
            // TODO: add another advanced search that allows partial/startswith/endswith/wildcard on username, fullname, email
            UserNameTermQuery query = new UserNameTermQuery(partialName, TermQuery.SUBSTRING_STARTS_WITH);
            SearchResult result = userAccessor.findUsers(query);
            users = PagerUtils.toList(result.pager());
        }
        catch (EntityException e) {
            e.printStackTrace();
        }

        return users;
    }

    public List findUsers(AdvancedUserQuery advancedUserQuery) {
        List users = new ArrayList();

        if (advancedUserQuery.getPartialUserName() != null && !"".equals(advancedUserQuery.getPartialUserName()) &&
                advancedUserQuery.getUserNameSearchType() != null) {
            try {
                UserNameTermQuery query = new UserNameTermQuery(advancedUserQuery.getPartialUserName(), advancedUserQuery.getUserNameSearchType());
                SearchResult result = userAccessor.findUsers(query);
                users = UserUtil.findIntersectionOfUsers(users,PagerUtils.toList(result.pager()));
            }
            catch (EntityException e) {
                e.printStackTrace();
            }
        }

        if (advancedUserQuery.getPartialFullName() != null && !"".equals(advancedUserQuery.getPartialFullName()) &&
                advancedUserQuery.getFullNameSearchType() != null) {
            try {
                FullNameTermQuery query = new FullNameTermQuery(advancedUserQuery.getPartialFullName(), advancedUserQuery.getFullNameSearchType());
                SearchResult result = userAccessor.findUsers(query);
                users = UserUtil.findIntersectionOfUsers(users,PagerUtils.toList(result.pager()));
            }
            catch (EntityException e) {
                e.printStackTrace();
            }
        }

        if (advancedUserQuery.getPartialEmail() != null && !"".equals(advancedUserQuery.getPartialEmail()) &&
                advancedUserQuery.getEmailSearchType() != null) {
            try {
                EmailTermQuery query = new EmailTermQuery(advancedUserQuery.getPartialEmail(), advancedUserQuery.getEmailSearchType());
                SearchResult result = userAccessor.findUsers(query);
                users = UserUtil.findIntersectionOfUsers(users,PagerUtils.toList(result.pager()));
            }
            catch (EntityException e) {
                e.printStackTrace();
            }
        }

        if (advancedUserQuery.getPartialGroupName() != null && !"".equals(advancedUserQuery.getPartialGroupName()) &&
                advancedUserQuery.getGroupNameSearchType() != null) {
            try {
                GroupNameTermQuery query = new GroupNameTermQuery(advancedUserQuery.getPartialGroupName(), advancedUserQuery.getGroupNameSearchType());
                SearchResult result = userAccessor.findGroups(query);
                List groups = PagerUtils.toList(result.pager());
                List usersOfAllMatchingGroups = new ArrayList();
                for (int i = 0; i < groups.size(); i++) {
                    usersOfAllMatchingGroups.addAll(findUsersForGroup((Group) groups.get(i)));
                }
                users = UserUtil.findIntersectionOfUsers(users,usersOfAllMatchingGroups); 
            }
            catch (EntityException e) {
                e.printStackTrace();
            }
        }

        return users;
    }

    public List addUserToGroup(User user, Group group) {
        return null;
    }

    public List removeUserFromGroup(User user, Group group) {
        return null;
    }


    public UserAccessor getUserAccessor() {
        return userAccessor;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }
}
