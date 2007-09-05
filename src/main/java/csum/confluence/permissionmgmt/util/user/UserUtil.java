/**
 * Copyright (c) 2007, Custom Space User Management Plugin Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Custom Space User Management Plugin Development Team
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package csum.confluence.permissionmgmt.util.user;

import com.atlassian.user.User;
import csum.confluence.permissionmgmt.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Gary S. Weaver
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

    public static String getUserInfoAsString(User user) {
        String result = null;
        if (user != null) {
            result = "" + user.getName() + "," + user.getFullName() + "," + user.getEmail();
        }
        else {
            result = "(null user)";
        }
        return result;
    }

    public static List findIntersectionOfUsers(List list1, List list2)
    {
        HashMap resultMap = new HashMap();
        for (int i=0; i<list1.size(); i++) {
            User user1 = (User)list1.get(i);
            for (int j=0; j<list2.size(); j++) {
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
