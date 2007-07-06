package raju.kadam.confluence.permissionmgmt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 6, 2007
 * Time: 9:09:06 AM
 */
public class PropsUtil {

    public static final String PROPS_FILENAME  = "spaceusergroupmanagementplugin.properties";

    private static Log log = LogFactory.getLog(PropsUtil.class);


    public static String getProperty(String propertyName) throws IOException {
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
                    t.printStackTrace();
                }
            }
        }

        return value;
    }
}
