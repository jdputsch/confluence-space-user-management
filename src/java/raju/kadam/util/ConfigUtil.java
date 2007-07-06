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

    public static String getTrimmedStringOrNull( String value ) {
        String result = value;
        if ( result != null ) {
            result = value.trim();
        }

        return result;
    }

    public static String getTrimmedStringOrUseDefaultIfValueIsNullOrTrimmedValueIsEmpty( String name, String value, String defaultValue ) {
        String result = defaultValue;
        if ( value != null ) {
            result = value.trim();
            if ( "".equals(value) ) {
                result = defaultValue;
            }
        }

        return result;
    }

    public static int getIntOrUseDefaultIfNullOrTrimmedValueIsEmptyOrNotAnInteger( String name, String value, int defaultValue ) {
        int result = defaultValue;
        if ( value != null ) {
            value = value.trim();
            try
            {
                result = Integer.parseInt(value);
            }
            catch(NumberFormatException nfe)
            {
                log.debug("Could not parse " + name + " value of '" + value +
                        "'. Using default value " + defaultValue );
            }
        }
        
        return result;
    }

    public static boolean isNullOrEmpty(String s) {
        boolean result = false;
        if (s==null || "".equals(s)) {
            result = true;
        }
        return result;
    }

    public static boolean isNotNullAndIsYesOrNo(String s) {
        boolean result = false;
        if ( s != null && ("YES".equals(s) || "NO".equals(s))) {
            result = true;
        }
        return result;
    }

    public static boolean isIntGreaterThanZero(String s) {
        boolean result = false;
        if ( s != null ) {
            int i = 0;
            try {
                i = Integer.parseInt(s);
                if (i>0) {
                    result = true;
                }
            }
            catch (NumberFormatException nfe) {
                // invalid
            }
        }
        return result;
    }        
}
