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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class StringUtil {

    private static final Log log = LogFactory.getLog(StringUtil.class);

    public static final String CHARACTERS_TO_CLEAN = "[<>/]";
    public static final String DELIMITING_CHARACTERS = "[:;,]";

    public static String clean(String s) {
        return s.replaceAll(CHARACTERS_TO_CLEAN, "");
    }

    public static boolean isNullOrEmpty(String s) {
        if (s == null || "".equals(s)) {
            return true;
        }
        return false;
    }

    public static List getCleanedListFromDelimitedValueString(String s) {
        List result = null;
        if (s != null) {
            result = new ArrayList();
            s = s.replaceAll(CHARACTERS_TO_CLEAN, "");
            String[] valueArray = s.split(DELIMITING_CHARACTERS);
            for (int i = 0; i < valueArray.length; i++) {
                String value = valueArray[i];
                if (value != null) {
                    value = value.trim();
                    if (!"".equals(value)) {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }

    public static String convertCollectionToCommaDelimitedString(Collection collection) {
        StringBuffer sb = new StringBuffer();
        if (collection != null) {
            Iterator iter = collection.iterator();
            int count = 0;
            while (iter.hasNext()) {
                String item = (String) iter.next();
                if (count != 0) {
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
        if (s1 == s2) {
            // note this checks for null==null as well as instance ref is same as instance ref
            result = true;
        } else if (s1 == null || s2 == null) {
            //implied result = false;
        } else if (s1.equals(s2)) {
            return true;
        }
        return result;
    }
}
