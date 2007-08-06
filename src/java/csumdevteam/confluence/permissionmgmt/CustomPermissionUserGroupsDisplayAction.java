/**
 * Copyright (c) 2007, Custom Space Usergroups Manager Development Team
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
 *     * Neither the name of the Custom Space Usergroups Manager Development Team
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

package csumdevteam.confluence.permissionmgmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.user.User;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;

/**
 * @author Rajendra Kadam
 */
public class CustomPermissionUserGroupsDisplayAction extends AbstractSpaceAction
{
	private String selectedGroupName = null;
	
	public String doDefault() throws Exception
    {
		return super.doDefault();
    }	

    public String getSelectedGroupName()
    {
    	return selectedGroupName;
    }
    
    public void setSelectedGroupName(String selectedGroupName)
    {
    	this.selectedGroupName =selectedGroupName;
    }
	
    //Get total user count for given user group
    public int findUserCountForUserGroup(String grpName)
    {
    	return PagerUtils.count(userAccessor.getMemberNames(userAccessor.getGroup(grpName)));
    }

    //If functions are getting called from Velocity, then it's better their names don't start with "get" as we seen sometimes those functions don't get call! 
    public List findUserDetailsForAGroup(String groupName)
    {
    	log.debug("group name -" + groupName);
		Pager usernames = userAccessor.getMemberNames(userAccessor.getGroup(groupName));
		Iterator iterator = usernames.iterator();
		
		List users = new ArrayList(findUserCountForUserGroup(groupName));
        while(iterator.hasNext())
        {
            Object next = iterator.next();
            User user = null;
            if (next instanceof User)
                user = (User) next;
            else
                user = userAccessor.getUser((String) next);

            // we don't know if group users are active or not, so check.
            if (groupName != null && userAccessor.isDeactivated(user))
                continue;
            
            log.debug(user);
            users.add(user);
        }

        sortUsers(users);
        return users;
    }

	
	//copied from com.atlassian.confluence.extra.userlister.model.Userlist.java
	//and a bit customized!
    private void sortUsers(List users)
    {
        Collections.sort(users, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                User u1 = (User) o1;
                User u2 = (User) o2;
                if (u1 == null && u2 == null)
                    return 0;
                if (u2 == null)
                    return -1;
                if (u1 == null)
                    return 1;
                
                String name1 = u1.getFullName();
                String name2 = u2.getFullName();
                if (name1 == null)
                    name1 = u1.getName();
                if (name2 == null)
                    name2 = u2.getName();
                if (name1 == null || name2 == null)
                    throw new RuntimeException("Null user name");
                else
                    return name1.toLowerCase().compareTo(name2.toLowerCase());
            }
        });
    }

    public String getActionName(String fullClassName)
    {
    	return "Custom Space Usergroup Details Display";
    }
    
}