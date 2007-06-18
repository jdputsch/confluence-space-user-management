package raju.kadam.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 8, 2007
 * Time: 11:08:35 AM
 */
public class ConfigUtil {

    private static final Log log = LogFactory.getLog(ConfigUtil.class);

    public static int getIntOrUseDefault( String name, String value, int defaultInt ) {
        int result = defaultInt;
        if ( value != null ) {
            int maxUserIDLimit = 20;
                try
                {
                    result = Integer.parseInt(value);
                }
                catch(NumberFormatException nfe)
                {
                    log.debug("Could not parse " + name + " value of '" + value +
                            "'. Using default value " + defaultInt );
                    //This will happen only if we don't validate max UserIDLimit during configuration or user has changed value by modifying xml file (confluence-home/config/confluence-global.bandana.xml).
                    maxUserIDLimit = 20;
                }
        }
        
        return result;
    }
}
