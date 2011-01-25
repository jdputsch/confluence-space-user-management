package csum.confluence.permissionmgmt.service.impl;

import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;
import com.atlassian.user.search.page.Pager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserAndGroupManagementService {

    protected Log log = LogFactory.getLog(this.getClass());

    protected UserManager userManager;
    protected GroupManager groupManager;

    // autowired by constructor injection via Atlassian Plugin framework/OSGi.
    public UserAndGroupManagementService(UserManager userManager,
                                         GroupManager groupManager) {
        this.userManager = userManager;
        this.groupManager = groupManager;
    }

    public User getUser(String username) {
        User user = null;
        try {
            user = userManager.getUser(username);
        } catch (Throwable t) {
            log.error("Problem getting user '" + username + "'", t);
        }

        return user;
    }

    public boolean isReadOnly(Group group) {
        if (group == null) {
            log.warn("Attempted to check isReadOnly on null group. Returning false.");
            return true;
        }

        boolean result = false;
        try {
            result = groupManager.isReadOnly(group);
        } catch (Throwable t) {
            log.error("Problem checking isReadOnly status of group '" + group.getName() + "'. Will assume isn't read-only.", t);
        }

        return result;
    }

    public User addUser(String userName, String email, String fullName) {
        User user = null;
        try {
            user = userManager.createUser(userName);
            user.setEmail(email);
            user.setFullName(fullName);
        } catch (Throwable t) {
            log.error("Problem creating user '" + userName + "'", t);
        }
        return user;
    }

    public Group getGroup(String groupName) {
        Group group = null;
        try {
            group = groupManager.getGroup(groupName);
        } catch (Throwable t) {
            log.error("Problem getting group '" + groupName + "'", t);
        }
        return group;
    }

    public Group addGroup(String groupName) {
        Group group = null;
        try {
            group = groupManager.createGroup(groupName);
        } catch (Throwable t) {
            log.error("Problem creating group '" + groupName + "'", t);
        }
        return group;
    }

    public void removeGroup(Group group) {
        if (group == null) {
            log.warn("Attempted to delete null group. Ignoring.");
        } else {
            try {
                groupManager.removeGroup(group);
            } catch (Throwable t) {
                log.error("Problem removing group with name '" + group.getName() + "'", t);
            }
        }
    }

    public Pager getMemberNames(Group group) {
        Pager pager = null;
        if (group == null) {
            log.warn("Attempted to get members of null group. Ignoring.");
        } else {
            try {
                pager = groupManager.getMemberNames(group);
            } catch (Throwable t) {
                log.error("Problem getting members of group '" + group.getName() + "'", t);
            }
        }
        return pager;
    }

    public void addMembership(Group group, User user) {
        if (group == null) {
            log.warn("Attempted to add user to null group. Ignoring.");
        } else if (user == null) {
            log.warn("Attempted to add null user to group. Ignoring.");
        } else {
            try {
                groupManager.addMembership(group, user);
            } catch (Throwable t) {
                log.error("Problem adding user '" + user.getName() + "' to group '" + group.getName() + "'", t);
            }
        }
    }

    public void removeMembership(Group group, User user) {
        if (group == null) {
            log.warn("Attempted to remove user from null group. Ignoring.");
        } else if (user == null) {
            log.warn("Attempted to remove null user from group. Ignoring.");
        } else {
            try {
                groupManager.removeMembership(group, user);
            } catch (Throwable t) {
                log.error("Problem removing user '" + user.getName() + "' from group '" + group.getName() + "'", t);
            }
        }
    }
}
