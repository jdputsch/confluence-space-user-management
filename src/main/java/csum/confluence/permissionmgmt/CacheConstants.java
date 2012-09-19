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

package csum.confluence.permissionmgmt;

/**
 * @author Gary S. Weaver
 */
public interface CacheConstants {
    public static final String PLUGIN_SESSION_KEY_PREFIX = "SUSR";
    public static final String DELIMITER = ":";
    public static final String GROUPS_SESSION_KEY_SUFFIX = "groups";
    public static final String USERS_SESSION_KEY_SUFFIX = "users";
    public static final String SEARCH_RESULT_USERS_SESSION_KEY_SUFFIX = "searchresultusers";
    public static final String INDEX_SUFFIX = "-index";
    public static final String GROUPS_INDEX_SESSION_KEY_SUFFIX = GROUPS_SESSION_KEY_SUFFIX + INDEX_SUFFIX;
    public static final String USERS_INDEX_SESSION_KEY_SUFFIX = USERS_SESSION_KEY_SUFFIX + INDEX_SUFFIX;
    public static final String SEARCH_RESULT_USERS_INDEX_SESSION_KEY_SUFFIX = SEARCH_RESULT_USERS_SESSION_KEY_SUFFIX + INDEX_SUFFIX;
    public static final String ADVANCED_USER_QUERY_SESSION_KEY_SUFFIX = "advanceduserquery";
    public static final String REMOTE_USER_KEY = "remoteUser";
}
