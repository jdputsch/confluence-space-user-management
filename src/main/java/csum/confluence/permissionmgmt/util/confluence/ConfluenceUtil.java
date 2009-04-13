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

package csum.confluence.permissionmgmt.util.confluence;

import com.atlassian.spring.container.ContainerManager;
import com.atlassian.confluence.util.GeneralUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Gary S. Weaver
 * @author Andy Brook
 */
public class ConfluenceUtil {

    private static Log log = LogFactory.getLog(ConfluenceUtil.class);

    public static Object loadComponentWithRetry(String component) {
        Object result = null;
        int triesLeft = 5;
        while (result == null && triesLeft > 0) {
            log.debug("attempting to load '" + component + "'. tries left = " + triesLeft);
            result = loadComponentOrReturnNullCatchingThrowable(component);
            triesLeft--;

            if (result==null) {
                try {
                    // wait 0-1000 msec
                    log.debug("waiting before next try");
                    Thread.sleep((int)(Math.random() * 1000D));
                }
                catch (InterruptedException ie) {
                    //don't care
                }
            }
        }

        if (result==null) {
            //one last try

            // maybe this one will work, or maybe it will throw an error
            result = ContainerManager.getComponent(component);
            log.debug("successfully loaded " + component);
        }

        return result;
    }

    private static Object loadComponentOrReturnNullCatchingThrowable(String component) {
        Object result = null;
        try {
            result = ContainerManager.getComponent(component);
            log.debug("successfully loaded " + component);
        }
        catch (Throwable t) {
            //don't care
            log.debug("failed to load " + component);
        }
        return result;
    }

    /**
     * Method helps avoid more recent API changes, imperfect long term, but works for now.
     *
     * @param version
     * @return true if equal to or above, false otherwise.
     */
    public static boolean isConfluenceVersionEqualToOrAbove(String version)
    {
        boolean result = false;

        // following code contributed by Andy Brook, with small modifications from Gary Weaver. Thanks, Andy!
        String fullVersion = GeneralUtil.getVersionNumber();
        if (fullVersion==null) {
            log.warn("Could not determine Confluence version. Got null version number from GeneralUtil.getVersionNumber().");
        }
        else if (version==null) {
            log.warn("version passed into isConfluenceVersionEqualToOrAbove was null.");
        }
        else {
            VersionNumberComparator comp = new VersionNumberComparator();
            if (comp.compare(fullVersion, version) >= 0) {
                log.debug("Confluence " + fullVersion + " is greater than or equal to " + version + ".");
                result = false;
            }
            else {
                log.debug("Confluence " + fullVersion + " is less than " + version + ".");
            }
        }

        return result;
    }
}