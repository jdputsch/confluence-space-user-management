/**
 * Copyright (c) 2007, Custom Space Usergroups Manager Development Team
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
 *     * Neither the name of the Custom Space Usergroups Manager Development Team
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

package csum.confluence.permissionmgmt.util.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Rajendra Kadam
 */
public class Status {
	private static final Log LOG = LogFactory.getLog(Status.class);

    public boolean isTaskDone;
    public String message;
    public Vector vNotCreatedUsers;
    public Vector vNotUsedGroups;

    public Status(boolean isTaskDone, String message, Vector vNotCreatedUsers, Vector vNotUsedGroups) {
        this.isTaskDone = isTaskDone;
        this.message = message;
        this.vNotCreatedUsers = vNotCreatedUsers;
        this.vNotUsedGroups = vNotUsedGroups;
    }

    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append("Operation Result - " + isTaskDone + "\n")
                .append("Operation Details - " + message + "\n")

                .append("------Not created Users ---------\n");
        goEnumeration(vNotCreatedUsers, output);

        output.append("------Groups that are not in system ---------\n");
        goEnumeration(vNotUsedGroups, output);

        return output.toString();
    }

    public void goEnumeration(Vector vec, StringBuffer output) {
        if (vec != null) {
            Enumeration e = vec.elements();
            while (e.hasMoreElements()) {
                output.append((String) e.nextElement() + "\n");
            }
        }
    }
}
