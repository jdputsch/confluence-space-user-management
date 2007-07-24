package raju.kadam.confluence.permissionmgmt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import raju.kadam.confluence.permissionmgmt.CustomPermissionConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigurable;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 1:38:19 PM
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
