/**
 * Copyright (c) 2007-2011, Custom Space User Management Plugin Development Team
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

package csum.confluence.permissionmgmt.util.cache;

import com.atlassian.user.User;
import csum.confluence.permissionmgmt.CacheConstants;
import csum.confluence.permissionmgmt.util.SessionUtil;

/**
 * @author Gary S. Weaver
 */
public class CacheUtil implements CacheConstants {

    // generic

    public static void storeInPluginCache(String key, Object o) {
        SessionUtil.setSessionProperty(PLUGIN_SESSION_KEY_PREFIX + DELIMITER + key, o);
    }

    public static Object getFromPluginCache(String key) {
        return SessionUtil.getSessionProperty(PLUGIN_SESSION_KEY_PREFIX + DELIMITER + key);
    }

    public static void removeFromPluginCache(String key) {
        SessionUtil.removeSessionProperty(PLUGIN_SESSION_KEY_PREFIX + DELIMITER + key);
    }

    // specific

    public static void setRemoteUser(User user) {
        storeInPluginCache(REMOTE_USER_KEY, user);
    }

    public static User getRemoteUser() {
        return (User) getFromPluginCache(REMOTE_USER_KEY);
    }
}
