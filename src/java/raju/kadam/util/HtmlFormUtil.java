package raju.kadam.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 22, 2007
 * Time: 11:09:22 AM
 */
public class HtmlFormUtil {


    /**
     * KEEPING THESE AROUND IN HERE IN CASE WE CAN REUSE LATER
     */
    // TODO: Finish v2 plugin and determine whether these will be used. If not, remove.

    //Helps to retrieve usergroups selected by User - removed "groups_" from selected checkbox name
    public static String buildUserGroupsFromCheckboxName(String checkboxName) {
        String[] splitUpCheckboxName = checkboxName.split("_", 2);
        return splitUpCheckboxName[1];
    }

    //Get the list of user groups that user has selected
    public static List retrieveListOfSelectedUserGroups(Map paramMap) {
        List selectedUserGroupsList = new ArrayList(4);

        //Get all groups that user has selected.
        for (Iterator iterator = paramMap.keySet().iterator(); iterator.hasNext();) {
            String paramKey = (String) iterator.next();
            if (paramKey != null && paramKey.startsWith("groups_")) {
                selectedUserGroupsList.add(buildUserGroupsFromCheckboxName(paramKey));
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
