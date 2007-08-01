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

package raju.kadam.confluence.permissionmgmt.util.group;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import raju.kadam.confluence.permissionmgmt.CustomPermissionConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigurable;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class GroupNameUtil {

    private static Log log = LogFactory.getLog(GroupNameUtil.class);


    public static String replaceSpaceKey(String groupPattern, String spaceKey) {
        log.debug("replaceSpaceKey() called.");
        //If spacekey is present in groupPattern then before compiling it replace it with current space key
        if (groupPattern.indexOf(CustomPermissionConstants.SPACEKEY) != -1) {
            //Replace String "SPACEKEY" with input Space Key.
            groupPattern = groupPattern.replaceFirst(CustomPermissionConstants.SPACEKEY, spaceKey);
        }

        return groupPattern;
    }

    public static Pattern createGroupMatchingPattern(CustomPermissionConfigurable config, String spaceKey)
    {
        log.debug("createGroupMatchingPattern() called.");
        String groupPattern = config.getUserGroupsMatchingPattern();
        if (groupPattern == null || (groupPattern.trim().equals(""))) {
            //This will only happen if we don't validate matching pattern during configuration.
            groupPattern = CustomPermissionConstants.SPACEKEY_REGEXP;
        }

        // NOTE: spaceKey converted to lowercase here and it is expected that the groupPattern with the exception of
        //       CustomPermissionConstants.SPACEKEY_REGEXP does not expect to match any groupname containing capital
        //       letters (as stated in the config page).
        groupPattern = replaceSpaceKey(groupPattern, spaceKey.toLowerCase());

        log.debug("group pattern -> " + groupPattern);

        Pattern pat = Pattern.compile(groupPattern);

        return pat;
    }

    public static boolean doesGroupMatchPattern(String grpName, Pattern pat) {
        boolean isMatch = false;
        if (grpName != null) {
            // NOTE: grpName converted to lowercase here. this means that 
            Matcher matcher = pat.matcher(grpName.toLowerCase());
            isMatch = matcher.matches();
        }

        return isMatch;
    }
}
