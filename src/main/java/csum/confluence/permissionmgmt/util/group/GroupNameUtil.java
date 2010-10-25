/**
 * Copyright (c) 2007-2010, Custom Space User Management Plugin Development Team
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

package csum.confluence.permissionmgmt.util.group;

import csum.confluence.permissionmgmt.CustomPermissionConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class GroupNameUtil {

    private static Log log = LogFactory.getLog(GroupNameUtil.class);


    public static String replaceSpaceKey(String groupPattern, String spaceKey) {
        //log.debug("replaceSpaceKey() called.");
        //If spacekey is present in groupPattern then before compiling it replace it with current space key
        if ((groupPattern != null) && (groupPattern.indexOf(CustomPermissionConstants.SPACEKEY) != -1)) {
            //Replace String "SPACEKEY" with input Space Key.
            groupPattern = groupPattern.replaceFirst(CustomPermissionConstants.SPACEKEY, spaceKey);
        }

        return groupPattern;
    }

    public static boolean doesGroupMatchPattern(String groupName,
                                                String prefixWithReplacedSpaceKey,
                                                String suffixWithReplacedSpaceKey) {
        boolean isMatch = false;
        if (groupName != null) {
            String lowerCaseGroupName = groupName.toLowerCase();
            if (lowerCaseGroupName.startsWith(prefixWithReplacedSpaceKey.toLowerCase()) && lowerCaseGroupName.endsWith(suffixWithReplacedSpaceKey.toLowerCase())) {
                // NOTE: grpName converted to lowercase here. expecting
                isMatch = true;
            }
        }

        return isMatch;
    }
}
