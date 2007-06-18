package raju.kadam.confluence.vo;

import java.util.*;
import bucket.user.UserAccessor;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.search.page.Pager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 8, 2007
 * Time: 11:59:28 AM
 */
public class ConfGroupImpl implements ConfGroup {

    private final Log log = LogFactory.getLog(this.getClass());

    private UserAccessor userAccessor;
    private Group group;
    private List confUsers;


    public String getName() {
        String name = null;
        if (this.group!=null) {
            name = this.group.getName();
        }
        return name;
    }

    public List getConfUsers() {
        if (this.confUsers==null) {
            try {
                this.confUsers = retrieveConfUsers();
            }
            catch (Throwable t) {
                log.error("Problem retrieving conf users for group " + getName(), t);
            }
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private List retrieveConfUsers() {
        List list = new ArrayList();
        Pager pager = userAccessor.getLocalMembers(this.group);
        Iterator iter = pager.iterator();
        while (iter.hasNext()) {
            User user = (User)iter.next();
            list.add(user);
        }
        return list;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }
}
