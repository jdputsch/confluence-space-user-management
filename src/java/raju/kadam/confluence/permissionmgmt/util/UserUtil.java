package raju.kadam.confluence.permissionmgmt.util;

import com.atlassian.user.User;
import raju.kadam.util.StringUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 21, 2007
 * Time: 1:20:00 PM
 */
public class UserUtil {

    // don't know whether equals() is implemented in atlassian User, so using this to check equality
    public static boolean areBothNullOrAreEqual(User user1, User user2) {
        boolean result = false;
        if (user1==user2) {
            // note this checks for null==null as well as instance ref is same as instance ref
            result = true;
        }
        else if (user1==null || user2==null) {
            //implied result = false;
        }
        else if (StringUtil.areBothNullOrAreEqual(user1.getName(), user2.getName()) &&
                StringUtil.areBothNullOrAreEqual(user1.getFullName(), user2.getFullName()) &&
                StringUtil.areBothNullOrAreEqual(user1.getEmail(), user2.getEmail()) )
        {
            result = true;
        }

        return result;
    }

    public static List findIntersectionOfUsers(List list1, List list2)
    {
        HashMap resultMap = new HashMap();
        for (int i=0; i<list1.size(); i++) {
            User user1 = (User)list1.get(i);
            for (int j=0; i<list2.size(); j++) {
                User user2 = (User)list2.get(j);
                if (areBothNullOrAreEqual(user1,user2)) {
                    // doesn't really matter which reference to use as data is same
                    resultMap.put(user1,"");
                }
            }
        }

        return new ArrayList(resultMap.keySet());
    }
}
