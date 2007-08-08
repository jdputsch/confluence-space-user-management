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

package csum.confluence.permissionmgmt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author Gary S. Weaver
 */
public class PropsUtil {

    public static final String PROPS_FILENAME  = "spaceusergroupmanagementplugin.properties";

    private static Log log = LogFactory.getLog(PropsUtil.class);


    public static String getProperty(String propertyName) throws IOException {
        log.debug("getProperty() called. propertyName='" + propertyName + "'");
        String value = null;
        InputStream in = null;
        try
        {
            log.debug("Loading property " + propertyName + " from properties file " + PROPS_FILENAME);
            Properties props = new Properties();
            in = PropsUtil.class.getClassLoader().getResourceAsStream(PROPS_FILENAME);
            if ( in != null ) {
                props.load(in);
                value = (String)props.get(propertyName);
            }

            if (value!=null) {
                log.debug("Loaded property " + propertyName );
            }
            else {
                log.warn("Failed to load property " + propertyName + " from properties file " + PROPS_FILENAME + " (was assuming it should be somewhere on classpath and property would be defined. see documentation for details)");
            }
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (Throwable t) {
                    log.error("Error closing props file input stream", t);
                }
            }
        }

        return value;
    }
}
