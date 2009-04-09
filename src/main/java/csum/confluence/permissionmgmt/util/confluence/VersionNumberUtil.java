package csum.confluence.permissionmgmt.util.confluence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Andy Brook
 * @author Gary S. Weaver
 */
public class VersionNumberUtil {

    private static Log log = LogFactory.getLog(ConfluenceUtil.class);

    public static Float getMajorAndMinorVersion(String fullVersion) {
        Float result = null;
        try {
            // eg 3.13.2
            if (fullVersion!=null) {
                String abridgedVersion = null;
                int idx1 = fullVersion.indexOf('.');
                if (idx1 != -1) {
                    int idx2 = fullVersion.substring(idx1 + 1).indexOf('.');
                    if (idx2 != -1)
                    {
                        abridgedVersion = fullVersion.substring(0, idx1 + 1 + idx2);
                    }
                    else
                    {
                        abridgedVersion = fullVersion;
                    }
                }

                result = new Float(abridgedVersion);
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("Supplied version number was null, so returning null");
                }
            }
        }
        catch (Throwable t) {
            log.error("Failed to parse version number '" + fullVersion + "'", t);
        }

        return result;
    }
}
