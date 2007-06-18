package raju.kadam.confluence.vo;

import com.atlassian.user.User;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 8, 2007
 * Time: 1:10:31 PM
 */
public class ConfUserImpl implements ConfUser {

    private User user;

    public String getName() {
        String name = null;
        if (this.user!=null) {
            name = this.user.getName();
        }
        return name;
    }

    public String getFullName() {
        String fullName = null;
        if (this.user!=null) {
            fullName = this.user.getFullName();
        }
        return fullName;
    }

    public String getEmail() {
        String email = null;
        if (this.user!=null) {
            email = this.user.getEmail();
        }
        return email;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
