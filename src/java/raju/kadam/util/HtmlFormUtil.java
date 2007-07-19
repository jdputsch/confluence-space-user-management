package raju.kadam.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 22, 2007
 * Time: 11:09:22 AM
 */
public class HtmlFormUtil {

    private static final Log log = LogFactory.getLog(HtmlFormUtil.class);

    /**
     * KEEPING THESE AROUND IN HERE IN CASE WE CAN REUSE LATER
     */
    // TODO: Finish v2 plugin and determine whether these will be used. If not, remove.

    //Helps to retrieve usergroups selected by User - removed "groups_" from selected checkbox name
    public static String getCheckboxValueFromParamName(String checkboxName) {
        String[] splitUpCheckboxName = checkboxName.split("_", 2);
        return splitUpCheckboxName[1];
    }

    //Get the list of user groups that user has selected
    public static List retrieveListOfCheckedCheckboxValues(Map paramMap, String checkboxGroupName) {
        List selectedUserGroupsList = new ArrayList(4);

        //Get all groups that user has selected.
        for (Iterator iterator = paramMap.keySet().iterator(); iterator.hasNext();) {
            String paramKey = (String) iterator.next();
            if (paramKey != null && paramKey.startsWith( checkboxGroupName + "_")) {

                String encoded = getCheckboxValueFromParamName(paramKey);
                try {
                    String decoded = URLDecoder.decode(encoded, "UTF-8");
                    selectedUserGroupsList.add(decoded);
                }
                catch (UnsupportedEncodingException e) {
                    log.error("Checkbox group name '" + checkboxGroupName + "' contained encoded param value '" + encoded + "' that could not be URL decoded", e);
                }
            }
        }

        return selectedUserGroupsList;
    }

    private boolean isGroupSelected(Map paramMap) {
        boolean result = false;
        for (Iterator iterator = paramMap.keySet().iterator(); iterator.hasNext();) {
            String paramKey = (String) iterator.next();
            if (paramKey != null && paramKey.startsWith("groups_")) {
                result = true;
                break;
            }
        }
        return result;
    }


}
