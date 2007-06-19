package raju.kadam.confluence.permissionmgmt.util;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import raju.kadam.confluence.permissionmgmt.CustomPermissionConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import raju.kadam.confluence.permissionmgmt.config.CustomPermissionConfiguration;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 18, 2007
 * Time: 1:38:19 PM
 */
public class GroupMatchingUtil {

    private static Log log = LogFactory.getLog(GroupMatchingUtil.class);


    public static Pattern createGroupMatchingPattern(CustomPermissionConfiguration config, String spaceKey)
    {
        String groupPattern = config.getUserGroupsMatchingPattern();
        if (groupPattern == null || (groupPattern.trim().equals(""))) {
            //This will only happen if we don't validate matching pattern during configuration.
            groupPattern = CustomPermissionConstants.SPACEKEY_REGEXP;
        }

        //If spacekey is present in groupPattern then before compiling it replace it with current space key
        if (groupPattern.indexOf(CustomPermissionConstants.SPACEKEY) != -1) {
            //Replace String "SPACEKEY" with input Space Key.
            groupPattern = groupPattern.replaceFirst(CustomPermissionConstants.SPACEKEY, spaceKey);
        }

        log.debug("group pattern -> " + groupPattern);

        Pattern pat = Pattern.compile(groupPattern);

        return pat;
    }

    public static boolean doesGroupMatchPattern(String grpName, Pattern pat) {
        Matcher matcher = pat.matcher(grpName);
        boolean isMatch = matcher.matches();

        if (log.isDebugEnabled()) {
            String pattern = pat.pattern();
            if (isMatch) {
                log.debug("group '" + grpName + "' matches pattern " + pattern);
            }
            else {
                log.debug("group '" + grpName + "' did not match pattern " + pattern);
            }
        }

        return isMatch;
    }
}
