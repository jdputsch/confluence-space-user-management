package raju.kadam.confluence.permissionmgmt.service.impl;

import raju.kadam.confluence.permissionmgmt.service.UserManagementService;

import java.util.List;

import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.EntityException;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import com.atlassian.user.search.query.Query;
import com.atlassian.user.search.query.UserNameTermQuery;
import com.atlassian.user.search.query.TermQuery;
import com.atlassian.user.search.SearchResult;
import com.atlassian.confluence.user.UserAccessor;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Hits;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;

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

    public List findUsersForGroup(Group group) {
        Pager pager = userAccessor.getMemberNames(group);
        return PagerUtils.toList(pager);
    }

    public List findUsersWhoseNameStartsWith(String partialName) {

        List users = null;

        try {
            // TODO: consider using TermQuery.SUBSTRING_CONTAINS in diff search
            UserNameTermQuery query = new UserNameTermQuery( partialName, TermQuery.SUBSTRING_STARTS_WITH );
            SearchResult result = userAccessor.findUsers(query);
            users = PagerUtils.toList(result.pager());
        }
        catch (EntityException e) {
            e.printStackTrace();
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
