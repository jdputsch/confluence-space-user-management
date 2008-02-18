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

package csum.confluence.permissionmgmt.service.impl;

import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.user.Group;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.spring.container.ContainerManager;
import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.service.exception.AddException;
import csum.confluence.permissionmgmt.service.exception.RemoveException;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.group.GroupNameUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class ConfluenceGroupManagementService extends BaseGroupManagementService {

    private SpacePermissionManager spacePermissionManager;

    public ConfluenceGroupManagementService() {
        setSpacePermissionManager((SpacePermissionManager) ContainerManager.getComponent("spacePermissionManager"));
    }

    public void addGroups(List groupNames, ServiceContext context) throws AddException {
        log.debug("addGroups() called. groupName='" + StringUtil.convertCollectionToCommaDelimitedString(groupNames) + "'");
        Space space = context.getSpace();

        List success = new ArrayList();
        List alreadyExisted = new ArrayList();

        for (int i = 0; i < groupNames.size(); i++) {
            String groupName = (String) groupNames.get(i);
            if (userAccessor.getGroup(groupName) == null) {

                Group vGroup = userAccessor.addGroup(groupName);
                log.debug("created " + groupName);
                success.add(groupName);

                //If group exists then set all required permissions
                if (vGroup != null) {
                    SpacePermission perm = new SpacePermission(SpacePermission.VIEWSPACE_PERMISSION, space, vGroup.getName());
                    space.addPermission(perm);
                    log.debug("added viewspace perm to " + groupName);
                }
            } else {
                alreadyExisted.add(groupName);
            }
        }

        if (alreadyExisted.size() > 0) {
            String msg = "";
            String concat = "";
            if (alreadyExisted.size() > 0) {
                msg += context.getText("csum.manager.error.groupsalreadyexisted") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(alreadyExisted) + ".";
                concat = " ";
            }

            if (success.size() > 0) {
                msg += concat;
                msg += context.getText("csum.manager.error.addgroupspartialsuccess") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(success) + ".";
            }

            throw new AddException(msg);
        }
    }

    public void removeGroups(List groupNames, ServiceContext context) throws RemoveException {
        log.debug("removeGroup() called. groupNames are " + StringUtil.convertCollectionToCommaDelimitedString(groupNames));
        List didNotExist = new ArrayList();
        List badGroupNames = new ArrayList();
        List success = new ArrayList();

        CustomPermissionConfiguration config = getCustomPermissionConfiguration();
        String spaceKey = context.getSpace().getKey();
        String prefix = GroupNameUtil.replaceSpaceKey(config.getNewGroupNameCreationPrefixPattern(), spaceKey);
        String suffix = GroupNameUtil.replaceSpaceKey(config.getNewGroupNameCreationSuffixPattern(), spaceKey);

        //Remove Selected Groups
        for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
            String grpName = (String) iterator.next();
            boolean isPatternMatch = GroupNameUtil.doesGroupMatchPattern(grpName, prefix, suffix);

            // Space admin should not be able to delete any groups whose names begin with "confluence"
            if (!grpName.startsWith("confluence") && isPatternMatch) {
                Group group = userAccessor.getGroup(grpName);
                if (group != null) {
                    removeGroup_Confluence2_6_0Compatible(group);
                    success.add(grpName);
                } else {
                    didNotExist.add(grpName);
                }
            } else {
                log.debug("Not deleting group '" + grpName + "', as either it started with 'confluence', didn't start with '" + prefix + "', or didn't end with '" + suffix + "'");
                badGroupNames.add(grpName);
            }
        }

        // if we failed, throw exception
        if (badGroupNames.size() > 0 || didNotExist.size() > 0) {
            String msg = "";
            String concat = "";
            if (badGroupNames.size() > 0) {
                msg += context.getText("csum.manager.error.badgroupnames") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(badGroupNames) + ".";
                concat = " ";
            }

            if (didNotExist.size() > 0) {
                msg += concat;
                msg += context.getText("csum.manager.error.groupsdidnotexist") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(didNotExist) + ".";
            }

            if (success.size() > 0) {
                msg += concat;
                msg += context.getText("csum.manager.error.removegroupspartialsuccess") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(success) + ".";
            }
            throw new RemoveException(msg);
        }
    }

    private void removeGroup_Confluence2_6_0Compatible(Group group) {
        if (group!=null) {
            // Workaround for http://jira.atlassian.com/browse/CONF-9623
            // Confluence 2.6.0 has a bug where removeGroup fails if space permissions are not removed for the group
            // prior to deletion of the group. This was not a problem in Confluence 2.5.x

            log.debug("Removing space permissions from group " + group.getName() + " as (workaround for CONF-9623)");
            log.debug("Calling spacePermissionManager.getAllPermissionsForGroup(" + group.getName() + ")");
            List perms = spacePermissionManager.getAllPermissionsForGroup(group.getName());
            if (perms!=null) {
                for (int i=0; i<perms.size(); i++) {
                    SpacePermission perm = (SpacePermission)perms.get(i);
                    log.debug("Calling spacePermissionManager.removePermission(" + perm + ")");
                    spacePermissionManager.removePermission(perm);
                }
            }
            log.debug("Calling userAccessor.removeGroup(...)");
            userAccessor.removeGroup(group);
        }
    }

    public SpacePermissionManager getSpacePermissionManager() {
        return spacePermissionManager;
    }

    public void setSpacePermissionManager(SpacePermissionManager spacePermissionManager) {
        this.spacePermissionManager = spacePermissionManager;
    }
}
