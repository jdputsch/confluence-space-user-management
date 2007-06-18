package raju.kadam.util;

import java.util.List;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 8, 2007
 * Time: 2:11:23 PM
 */
public class ListUtil {

    public static String convertListToCommaDelimitedString( List list ) {
        StringBuffer sb = new StringBuffer();
        if ( list != null ) {
            for ( int i=0; i<list.size(); i++ ) {
                String item = (String)list.get(i);
                if (i!=0) {
                    sb.append(", ");
                }
                sb.append(item);
            }
        }
        return sb.toString();
    }

    public static boolean isListSizeOverMaxNum( List list, int max ) {
        boolean result = false;
        if ( list != null && list.size() > max ) {
            result = true;
        }
        return result;
    }        
}
