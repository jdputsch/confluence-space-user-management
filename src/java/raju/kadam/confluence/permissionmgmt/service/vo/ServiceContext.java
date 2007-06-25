package raju.kadam.confluence.permissionmgmt.service.vo;

import com.atlassian.confluence.spaces.Space;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 22, 2007
 * Time: 8:57:05 AM
 */
public class ServiceContext {

    String loggedInUser;
    Space space;


    public String getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(String loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }
}
