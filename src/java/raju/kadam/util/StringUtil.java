package raju.kadam.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
/**
 *
 * @author Gary S. Weaver
 */
public class StringUtil {

    private static final Log log = LogFactory.getLog(StringUtil.class);

    public static final String CHARACTERS_TO_CLEAN = "[<>/]";
    public static final String DELIMITING_CHARACTERS = "[:;,]";

    public static String clean( String s ) {
        return s.replaceAll(CHARACTERS_TO_CLEAN, "");
    }

    public static boolean isNullOrEmpty(String s) {
        if (s == null || "".equals(s)) {
            return true;
        }
        return false;
    }

    public static List getCleanedListFromDelimitedValueString( String s ) {
        List result = null;
        if ( s != null ) {
            result = new ArrayList();
            s = s.replaceAll(CHARACTERS_TO_CLEAN, "");
            String[] valueArray = s.split(DELIMITING_CHARACTERS);
            for (int i=0; i<valueArray.length; i++) {
                String value = valueArray[i];
                if (value!=null) {
                    value = value.trim();
                    value = value.toLowerCase();
                    if (!"".equals(value)) {
                        result.add(value);
                    }
                }
            }
        }
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

    public static boolean areBothNullOrAreEqual(String s1, String s2) {
        boolean result = false;
        if (s1==s2) {
            // note this checks for null==null as well as instance ref is same as instance ref
            result = true;
        }
        else if (s1==null || s2==null) {
            //implied result = false;
        }
        else if (s1.equals(s2)) {
            return true;
        }
        return result;
    }     
}
