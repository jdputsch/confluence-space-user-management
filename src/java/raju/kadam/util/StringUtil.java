package raju.kadam.util;

import java.util.*;
/**
 *
 * @author Gary S. Weaver
 */
public class StringUtil {

    public static List convertDelimitedStringToCleanedLowercaseList( String s ) {
        List result = null;
        if ( s != null ) {
            s = s.toLowerCase().trim();
            s = s.replaceAll("[<>/]", "");
            result = convertColonSemicolonOrCommaDelimitedStringToList(s);
        }
        return result;
    }

    public static List convertColonSemicolonOrCommaDelimitedStringToList( String s ) {
        String[] valueArray = s.split("[:;,]");
        List result = Arrays.asList(valueArray);
        return result;
    }
}
