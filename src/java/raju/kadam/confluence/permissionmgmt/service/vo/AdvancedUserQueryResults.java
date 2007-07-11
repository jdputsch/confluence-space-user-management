package raju.kadam.confluence.permissionmgmt.service.vo;

import org.displaytag.pagination.PaginatedList;
import com.atlassian.user.search.page.Pager;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 3, 2007
 * Time: 2:00:39 PM
 */
public class AdvancedUserQueryResults {

    private Pager users;
    private String message;


    public Pager getUsers() {
        return users;
    }

    public void setUsers(Pager users) {
        this.users = users;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
