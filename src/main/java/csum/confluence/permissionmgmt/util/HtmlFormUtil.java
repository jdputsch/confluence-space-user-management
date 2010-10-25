/**
 * Copyright (c) 2007-2010, Custom Space User Management Plugin Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Custom Space User Management Plugin Development Team
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package csum.confluence.permissionmgmt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class HtmlFormUtil {

    private static final Log log = LogFactory.getLog(HtmlFormUtil.class);

    /**
     * KEEPING THESE AROUND IN HERE IN CASE WE CAN REUSE LATER
     */
    // TODO: Finish v2 plugin and determine whether these will be used. If not, remove.

    //Helps to retrieve User selected by User - removed "groups_" from selected checkbox name
    public static String getCheckboxValueFromParamName(String checkboxName) {
        String[] splitUpCheckboxName = checkboxName.split("_", 2);
        return splitUpCheckboxName[1];
    }

    //Get the list of user groups that user has selected
    public static List retrieveListOfCheckedCheckboxValues(Map paramMap, String checkboxGroupName) {
        List selectedUserList = new ArrayList(4);

        //Get all groups that user has selected.
        for (Iterator iterator = paramMap.keySet().iterator(); iterator.hasNext();) {
            String paramKey = (String) iterator.next();
            if (paramKey != null && paramKey.startsWith(checkboxGroupName + "_")) {

                String encoded = getCheckboxValueFromParamName(paramKey);
                try {
                    String decoded = URLDecoder.decode(encoded, "UTF-8");
                    selectedUserList.add(decoded);
                }
                catch (UnsupportedEncodingException e) {
                    // NOTE: this should allow legal groupnames to pass so just log and ignore
                    log.debug("Checkbox group name '" + checkboxGroupName + "' contained encoded param value '" + encoded + "' that could not be URL decoded", e);
                }
                catch (IllegalArgumentException e) {
                    //TODO: write test for groupname sdfaa#$%#$%$%&
                    // NOTE: this should allow legal groupnames to pass so just log and ignore
                    log.debug("Checkbox group name '" + checkboxGroupName + "' contained encoded param value '" + encoded + "' that could not be URL decoded", e);
                }
            }
        }

        return selectedUserList;
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
