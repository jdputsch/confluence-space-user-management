package raju.kadam.util;

import java.util.List;
import java.util.ArrayList;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 8, 2007
 * Time: 2:11:23 PM
 */
public class ListUtil {

    public static boolean isListSizeOverMaxNum( List list, int max ) {
        boolean result = false;
        if ( list != null && list.size() > max ) {
            result = true;
        }
        return result;
    }

    // TODO: hopefully this goes away when we decide for sure whether we will support multiple users->group actions at once
    public static List createListOfOneItem( Object item ) {
        List list = new ArrayList();
        list.add(item);
        return list;
    }
}
