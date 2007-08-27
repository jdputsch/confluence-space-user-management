package csum.confluence.permissionmgmt.config;

import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.bandana.BandanaManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 27, 2007
 * Time: 10:04:02 AM
 */
public class LegacyConfigurationMigrator {

    private static final Log log = LogFactory.getLog(LegacyConfigurationMigrator.class);

    // character that could indicate presence of regexp. See: http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html
    private static final String LEGACY_REGEXP_IDENTIFIERS = "[]\\+.*?{}|()&:";

    public static void migrateLegacyConfiguration(BandanaManager bandanaManager) {
        try {
            migrateMatchingPatternToGroupPrefixAndSuffix(bandanaManager);
            migrateMaxUserIDsToMaxGroupIDs(bandanaManager);
        }
        catch (Throwable t) {
            log.error("Problem migrating old plugin config (not critical)", t);
        }
    }

    private static void migrateMatchingPatternToGroupPrefixAndSuffix(BandanaManager bandanaManager) {
        String newGroupPrefix = getValue(bandanaManager, CustomPermissionConfigConstants.DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_PREFIX_PATTERN);
        String newGroupSuffix = getValue(bandanaManager, CustomPermissionConfigConstants.DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_SUFFIX_PATTERN);

        if (newGroupPrefix == null && newGroupSuffix == null) {
            String legacyMatchingPattern = getValue(bandanaManager, LegacyConfigConstants.DELEGATE_USER_MGMT_USERGROUPS_MATCHING_PATTERN);
            if ( legacyMatchingPattern!=null ) {
                // this is used as an example in old config page
                int wildCardIndex = legacyMatchingPattern.indexOf(".*");
                if (wildCardIndex == -1) {
                    // this is used as an example in online config documentation
                    wildCardIndex = legacyMatchingPattern.indexOf(".+");
                }

                if (wildCardIndex != -1) {
                    newGroupPrefix = legacyMatchingPattern.substring(0, wildCardIndex);
                    newGroupSuffix = legacyMatchingPattern.substring(2);

                    if (newGroupPrefix != null && !hasRegExpRemnant(newGroupPrefix)) {
                        setValue(bandanaManager, CustomPermissionConfigConstants.DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_PREFIX_PATTERN, newGroupPrefix);
                    }

                    if (newGroupSuffix != null && !hasRegExpRemnant(newGroupSuffix)) {
                        setValue(bandanaManager, CustomPermissionConfigConstants.DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_SUFFIX_PATTERN, newGroupSuffix);
                    }
                }
            }

        }
    }

    private static void migrateMaxUserIDsToMaxGroupIDs(BandanaManager bandanaManager) {
        String maxGroupIDs = getValue(bandanaManager, CustomPermissionConfigConstants.DELEGATE_USER_MGMT_MAXGROUPIDS_LIMIT);

        if (maxGroupIDs == null) {
            String legacyMaxUserIDs = getValue(bandanaManager, LegacyConfigConstants.DELEGATE_USER_MGMT_MAXUSERIDS_LIMIT);
            if ( legacyMaxUserIDs!=null ) {
                setValue(bandanaManager, CustomPermissionConfigConstants.DELEGATE_USER_MGMT_MAXGROUPIDS_LIMIT, legacyMaxUserIDs);
            }
        }
    }

    private static boolean hasRegExpRemnant(String s) {
        boolean result = false;

        if (s.contains(LEGACY_REGEXP_IDENTIFIERS)) {
            result = true;
        }
        return result;
    }



    public static String getValue(BandanaManager bandanaManager, String key) {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(), key);
    }

    public static void setValue(BandanaManager bandanaManager, String key, String value) {
        bandanaManager.setValue(new ConfluenceBandanaContext(), key, value);
    }
}
