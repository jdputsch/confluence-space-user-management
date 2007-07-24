package raju.kadam.confluence.permissionmgmt.util;

import com.atlassian.user.Group;

import java.util.Comparator;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 24, 2007
 * Time: 1:55:04 PM
 */
public class RangeComparator  implements Comparator {

    public int compare(Object o, Object o1)
    {
        return new Integer(((Range) o).getRecordNum()).compareTo(new Integer(((Range) o1).getRecordNum()));
    }
}
