/**
 * Copyright (c) 2007-2012, Custom Space User Management Plugin Development Team
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

package csum.confluence.permissionmgmt.util.paging;

import com.atlassian.user.User;
import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerException;
import csum.confluence.permissionmgmt.service.impl.UserAndGroupManagementService;
import csum.confluence.permissionmgmt.util.logging.LogUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Gary S. Weaver
 */
public class LazyLoadingUserByUsernamePager implements Pager {

    public final Log log = LogFactory.getLog(this.getClass());

    private Pager usernamePager;
    private UserAndGroupManagementService userAndGroupManagementService;

    public boolean isEmpty() {
        Pager usernamePager = getUsernamePager();
        if (usernamePager != null) {
            usernamePager.nextPage();
        }

        return getUsernamePager().isEmpty();
    }

    public Iterator iterator() {
        Pager usernamePager = getUsernamePager();
        if (usernamePager != null) {
            LazyLoadingUserByUsernamePagerIterator iterator = new LazyLoadingUserByUsernamePagerIterator();

            iterator.setUserAndGroupManagementService(getUserAndGroupManagementService());
            iterator.setUsernamePagerIterator(getUsernamePager().iterator());

            return iterator;
        }

        return new ArrayList().iterator();
    }

    public List getCurrentPage() {
        List results = new ArrayList();
        Pager usernamePager = getUsernamePager();
        // must check for null here! (SUSR-54)
        if (usernamePager != null) {
            List usernames = usernamePager.getCurrentPage();
            if (usernames != null) {
                for (int i = 0; i < usernames.size(); i++) {
                    String username = (String) usernames.get(i);
                    User user = getUserAndGroupManagementService().getUser(username);
                    results.add(user);
                }
            } else {
                log.debug("usernamePager.getCurrentPage() returned null. This may just be an empty group.");
            }
        } else {
            LogUtil.warnWithRemoteUserInfo(log, "usernamePager was null. Either was an empty group or an API issue. Look in debug logging for the groupname to look it up and be sure.");
        }
        return results;
    }

    public void nextPage() {
        Pager usernamePager = getUsernamePager();
        if (usernamePager != null) {
            usernamePager.nextPage();
        }
    }

    public boolean onLastPage() {
        Pager usernamePager = getUsernamePager();
        if (usernamePager != null) {
            return usernamePager.onLastPage();
        }

        return true;
    }

    public void skipTo(int i) throws PagerException {
        Pager usernamePager = getUsernamePager();
        if (usernamePager != null) {
            usernamePager.skipTo(i);
        }
    }

    public int getIndex() {
        Pager usernamePager = getUsernamePager();
        if (usernamePager != null) {
            return usernamePager.getIndex();
        }

        return 0;
    }

    public int getIndexOfFirstItemInCurrentPage() {
        Pager usernamePager = getUsernamePager();
        if (usernamePager != null) {
            return usernamePager.getIndexOfFirstItemInCurrentPage();
        }

        return 0;
    }

    public Pager getUsernamePager() {
        return usernamePager;
    }

    public void setUsernamePager(Pager usernamePager) {
        this.usernamePager = usernamePager;
    }

    public UserAndGroupManagementService getUserAndGroupManagementService() {
        return userAndGroupManagementService;
    }

    public void setUserAndGroupManagementService(UserAndGroupManagementService userAndGroupManagementService) {
        this.userAndGroupManagementService = userAndGroupManagementService;
    }
}
