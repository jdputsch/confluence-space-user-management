/**
* Copyright (c) 2007, Custom Space Usergroups Manager Development Team
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the Custom Space Usergroups Manager Development Team nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE CUSTOM SPACE USERGROUPS MANAGER DEVELOPMENT TEAM ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE CUSTOM SPACE USERGROUPS MANAGER DEVELOPMENT TEAM BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package raju.kadam.confluence.permissionmgmt.service;

import java.util.List;

import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQuery;
import raju.kadam.confluence.permissionmgmt.service.vo.ServiceContext;
import raju.kadam.confluence.permissionmgmt.service.vo.AdvancedUserQueryResults;
import raju.kadam.confluence.permissionmgmt.service.exception.AddException;
import raju.kadam.confluence.permissionmgmt.service.exception.FindException;
import raju.kadam.confluence.permissionmgmt.service.exception.RemoveException;
import com.atlassian.user.search.page.Pager;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 11:37:32 AM
 */
public interface UserManagementService {

    public AdvancedUserQueryResults findUsers( AdvancedUserQuery advancedUserQuery, ServiceContext context ) throws FindException;

    public Pager findUsersForGroup( String groupName, ServiceContext context ) throws FindException;

    public Pager findUsersWhoseNameStartsWith( String partialName, ServiceContext context ) throws FindException;

    public void addUsersByUsernameToGroups( List userNames, List groupNames, ServiceContext context ) throws AddException;

    public void removeUsersByUsernameFromGroups( List userNames, List groupNames, ServiceContext context ) throws RemoveException;

    public boolean isMemberOf(String userName, String groupName) throws FindException;
}
