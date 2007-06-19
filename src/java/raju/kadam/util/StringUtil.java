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

    public static String convertCollectionToCommaDelimitedString( Collection collection ) {
            StringBuffer sb = new StringBuffer();
            if ( collection != null ) {
                Iterator iter = collection.iterator();
                int count = 0;
                while (iter.hasNext()) {
                    String item = (String)iter.next();
                    if (count!=0) {
                        sb.append(", ");
                    }
                    sb.append(item);
                    count++;
                }
            }
            return sb.toString();
        }


}
