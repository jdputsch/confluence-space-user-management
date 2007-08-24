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
import csum.confluence.permissionmgmt.util.StringUtil;

/**
 * Wrapper class for retrieved LDAP attributes and optional formatting of name
 * @author Andy Brook
 */
public class LDAPUser
{
	private static final Log LOG = LogFactory.getLog(LDAPUser.class);
	
    String userId;
    String fullName;
    String email;
    String firstName;
    String lastName;
    
    public static final int LASTNAME_COMMA_FIRSTNAME=0;
    public static final int FIRSTNAME_SPACE_LASTNAME=1;
    
    /** default format lastname_comma_firstname */
    int fullNameFormat=LASTNAME_COMMA_FIRSTNAME;
    
    public LDAPUser() {
    }

    public LDAPUser(String userid, String fullName, String email) {
        this.userId = userid;
        this.fullName = fullName;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName()
    {
    	String name=null;

        if (!StringUtil.isNullOrEmpty(firstName) && !StringUtil.isNullOrEmpty(lastName)) {
            if (fullNameFormat==LASTNAME_COMMA_FIRSTNAME) {
                name = lastName + ", " + firstName;
            }
            else if (fullNameFormat==FIRSTNAME_SPACE_LASTNAME) {
                name = firstName + " " + lastName;
            }
            else {
                LOG.warn("Invalid format: " + fullNameFormat + ". Using FIRSTNAME_SPACE_LASTNAME format.");
                name = firstName + " " + lastName;
            }
        }
        else if (!StringUtil.isNullOrEmpty(firstName) && StringUtil.isNullOrEmpty(lastName)) {
            name = firstName;
        }
        else if (StringUtil.isNullOrEmpty(firstName) && !StringUtil.isNullOrEmpty(lastName)) {
            name = lastName;
        }
        else {
            name = userId;
        }

        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getFullNameFormat() {
		return fullNameFormat;
	}

	public void setFullNameFormat(int fullNameFormat) {
		this.fullNameFormat = fullNameFormat;
	}
}
