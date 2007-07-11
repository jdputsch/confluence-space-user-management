package raju.kadam.confluence.permissionmgmt.paging;

import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerException;
import com.atlassian.user.User;
import com.atlassian.confluence.user.UserAccessor;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 9, 2007
 * Time: 2:55:40 PM
 */
public class LazyLoadingUserByUsernamePager implements Pager {

    private Pager usernamePager;
    private UserAccessor userAccessor;

    public boolean isEmpty() {
        return getUsernamePager().isEmpty();
    }

    //TODO: Warning! This is not currently implemented to return User instead of username, so I think PagerUtils may not work...
    public Iterator iterator() {
        return getUsernamePager().iterator();
    }

    public List getCurrentPage() {
        List results = new ArrayList();
        List usernames = getUsernamePager().getCurrentPage();
        if (usernames!=null) {
            for (int i=0; i<usernames.size(); i++) {
                String username = (String)usernames.get(i);
                User user = getUserAccessor().getUser(username);
                results.add(user);
            }
        }
        return results;
    }

    public void nextPage() {
        getUsernamePager().nextPage();
    }

    public boolean onLastPage() {
        return getUsernamePager().onLastPage();
    }

    public void skipTo(int i) throws PagerException {
        getUsernamePager().skipTo(i);
    }

    public int getIndex() {
        return getUsernamePager().getIndex();
    }

    public int getIndexOfFirstItemInCurrentPage() {
        return getUsernamePager().getIndexOfFirstItemInCurrentPage();
    }

    public Pager getUsernamePager() {
        return usernamePager;
    }

    public void setUsernamePager(Pager usernamePager) {
        this.usernamePager = usernamePager;
    }

    public UserAccessor getUserAccessor() {
        return userAccessor;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }
}
