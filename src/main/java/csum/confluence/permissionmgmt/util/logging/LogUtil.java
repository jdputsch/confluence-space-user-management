/**
 * Copyright (c) 2007-2009, Custom Space User Management Plugin Development Team
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

package csum.confluence.permissionmgmt.util.logging;

import csum.confluence.permissionmgmt.util.cache.CacheUtil;
import csum.confluence.permissionmgmt.util.user.UserUtil;
import org.apache.commons.logging.Log;
import org.apache.log4j.Category;

/**
 * @author Gary S. Weaver
 */
public class LogUtil {

    private static String getUserInfoString() {
        return " (logged in user=" + UserUtil.getUserInfoAsString(CacheUtil.getRemoteUser()) + ")";
    }

    // log4j methods - unfortunately confluence still uses it instead of commons logging as of 2.5.7

    public static void fatalWithRemoteUserInfo(Category log, String message) {
        log.fatal(message + getUserInfoString());
    }

    public static void fatalWithRemoteUserInfo(Category log, String message, Throwable t) {
        log.fatal(message + getUserInfoString(), t);
    }

    public static void errorWithRemoteUserInfo(Category log, String message) {
        log.error(message + getUserInfoString());
    }

    public static void errorWithRemoteUserInfo(Category log, String message, Throwable t) {
        log.error(message + getUserInfoString(), t);
    }

    public static void warnWithRemoteUserInfo(Category log, String message) {
        log.warn(message + getUserInfoString());
    }

    public static void warnWithRemoteUserInfo(Category log, String message, Throwable t) {
        log.warn(message + getUserInfoString(), t);
    }

    public static void infoWithRemoteUserInfo(Category log, String message) {
        log.info(message + getUserInfoString());
    }

    public static void infoWithRemoteUserInfo(Category log, String message, Throwable t) {
        log.info(message + getUserInfoString(), t);
    }

    public static void debugWithRemoteUserInfo(Category log, String message) {
        log.info(message + getUserInfoString());
    }

    public static void debugWithRemoteUserInfo(Category log, String message, Throwable t) {
        log.info(message + getUserInfoString(), t);
    }

    // commons logging methods

    public static void fatalWithRemoteUserInfo(Log log, String message) {
        log.fatal(message + getUserInfoString());
    }

    public static void fatalWithRemoteUserInfo(Log log, String message, Throwable t) {
        log.fatal(message + getUserInfoString(), t);
    }

    public static void errorWithRemoteUserInfo(Log log, String message) {
        log.error(message + getUserInfoString());
    }

    public static void errorWithRemoteUserInfo(Log log, String message, Throwable t) {
        log.error(message + getUserInfoString(), t);
    }

    public static void warnWithRemoteUserInfo(Log log, String message) {
        log.warn(message + getUserInfoString());
    }

    public static void warnWithRemoteUserInfo(Log log, String message, Throwable t) {
        log.warn(message + getUserInfoString(), t);
    }

    public static void infoWithRemoteUserInfo(Log log, String message) {
        log.info(message + getUserInfoString());
    }

    public static void infoWithRemoteUserInfo(Log log, String message, Throwable t) {
        log.info(message + getUserInfoString(), t);
    }

    public static void debugWithRemoteUserInfo(Log log, String message) {
        log.debug(message + getUserInfoString());
    }

    public static void debugWithRemoteUserInfo(Log log, String message, Throwable t) {
        log.debug(message + getUserInfoString(), t);
    }
}
