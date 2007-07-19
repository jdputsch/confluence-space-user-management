package raju.kadam.confluence.permissionmgmt.util;

import com.atlassian.user.Group;

import java.util.Comparator;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 19, 2007
 * Time: 10:20:23 AM
 */
public class GroupComparator implements Comparator {

    public int compare(Object o, Object o1)
    {
        return ((Group) o).getName().compareToIgnoreCase(((Group) o1).getName());
    }
}
