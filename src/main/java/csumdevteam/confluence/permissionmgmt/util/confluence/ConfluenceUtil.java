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

package csumdevteam.confluence.permissionmgmt.util.confluence;

import com.atlassian.spring.container.ContainerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Gary S. Weaver
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
}