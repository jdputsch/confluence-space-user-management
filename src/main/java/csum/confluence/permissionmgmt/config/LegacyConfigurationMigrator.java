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

package csum.confluence.permissionmgmt.config;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import csum.confluence.permissionmgmt.util.logging.LogUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility to migrate what we can of the config from v1.1 of the plugin
 *
 * @author Gary S. Weaver
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
            LogUtil.errorWithRemoteUserInfo(log, "Problem migrating old plugin config (not critical)", t);
        }
    }

    private static void migrateMatchingPatternToGroupPrefixAndSuffix(BandanaManager bandanaManager) {
        String newGroupPrefix = getValue(bandanaManager, CustomPermissionConfigConstants.DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_PREFIX_PATTERN);
        String newGroupSuffix = getValue(bandanaManager, CustomPermissionConfigConstants.DELEGATE_USER_MGMT_NEW_GROUP_NAME_CREATION_SUFFIX_PATTERN);

        if (newGroupPrefix == null && newGroupSuffix == null) {
            String legacyMatchingPattern = getValue(bandanaManager, LegacyConfigConstants.DELEGATE_USER_MGMT_USER_MATCHING_PATTERN);
            if (legacyMatchingPattern != null) {
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
            if (legacyMaxUserIDs != null) {
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
