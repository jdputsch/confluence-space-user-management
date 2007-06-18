package raju.kadam.confluence.vo;

import java.util.*;
/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 8, 2007
 * Time: 12:39:49 PM
 */
public interface ConfGroup {

    /**
     * Gets the name of this group.
     * 
     * @return the name of this group
     */
    public String getName();

    /**
     * Gets the users that belong to this group.
     *
     * @return java.util.List of ConfUser instances
     */
    public List getConfUsers();
}
