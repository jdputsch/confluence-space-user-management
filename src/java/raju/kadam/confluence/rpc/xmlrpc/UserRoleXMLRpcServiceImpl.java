/*
Copyright (c) 2006, Rajendra Kadam
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.  Redistributions
in binary form must reproduce the above copyright notice, this list of
conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.  Neither the name of
Cenqua Pty Ltd nor the names of its contributors may be used to
endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package raju.kadam.confluence.rpc.xmlrpc;

import java.util.List;

import bucket.container.ContainerManager;
//import bucket.core.persistence.hibernate.HibernateConfig;
import com.atlassian.config.db.HibernateConfig;

import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.persistence.dao.SpaceDao;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.user.User;
import org.apache.log4j.Category;

/**
 *
 * @author Rajendra Kadam
 */
public class UserRoleXMLRpcServiceImpl implements UserRoleXMLRpcService
{
    private static final Category log = Category.getInstance(UserRoleXMLRpcServiceImpl.class);

    private SpaceDao spDao;
	private UserAccessor userAccessor; 
	private HibernateConfig hibernateConfig;
	
	public UserRoleXMLRpcServiceImpl()
	{
		spDao = (SpaceDao) ContainerManager.getComponent("spaceDao");
		userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");

/*	
 		hibernateConfig = (HibernateConfig) ContainerManager.getComponent("hibernateConfig");
		if(hibernateConfig != null)
		{
			Properties props = hibernateConfig.getHibernateProperties();
			Enumeration enume = props.keys();
			 while(enume.hasMoreElements()){
				 String key = (String)enume.nextElement();
				 log.debug("Key - "+ key + " value - " + props.getProperty(key));
			 }
		}
		
		log.debug(userAccessor.getUser("rakadam").getFullName());
*/	
	}

	public Boolean verifySpaceAdminRole(String userid, String spaceKey) 
	{
		boolean resultFlag = false;
		
		try{
	    	//Is there any better api we can use to verify if this logged in user is Space Administrator or not ?
			User usr = userAccessor.getUser(userid);
			
			//If Confluence Administrator return true! 
	        if (GeneralUtil.isSuperUser(usr))
	        {
	        	//log.debug("This is Super Admin...");
	        	return new Boolean(true);
	        }
	        
			Space sp = spDao.getSpace(spaceKey);
			//log.debug("Passed Space is - " + sp.getName());
			
			if(usr != null)
			{
		    	List list = getSpacesAssociatedToUserForGivenPermission( usr,SpacePermission.ADMINISTER_SPACE_PERMISSION);
		    	resultFlag = ((list != null && list.contains(sp)) ? true:false);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return new Boolean(resultFlag);
	}

    public List getAllSpaces()
    {
        return spDao.findAllSorted("name");
    }
	
    public List getSpacesAssociatedToUserForGivenPermission(User user, String permission)
    {
        List permittedSpacesForUser = spDao.getPermittedSpacesForUser(user,permission);
        return permittedSpacesForUser;
    }

}
