package raju.kadam.confluence.permissionmgmt.util;

import java.util.List;
import java.util.Collections;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 19, 2007
 * Time: 10:23:11 AM
 */
public class GroupUtil {

    // should do this in query instead
    public static void sortGroupsByGroupnameAscending(List groups) {
        Collections.sort(groups, new GroupComparator());
    }
}
