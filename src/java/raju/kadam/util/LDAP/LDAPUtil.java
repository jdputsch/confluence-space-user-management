/*
Copyright (c) 2006, Rajendra Kadam
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.  Redistributions
in binary form must reproduce the above copyright notice, this list of
conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.  Neither the name of
Cenqua Pty Ltd nor the names of its contributors may be used to
endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package raju.kadam.util.LDAP;

import org.apache.log4j.Logger;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;
import org.apache.log4j.Category;

public class LDAPUtil {

    private static final Category log = Category.getInstance(LDAPUtil.class);

    public final static String LDAP_CLASS = "com.sun.jndi.ldap.LdapCtxFactory";

    public static LDAPUser getLDAPUser(String userid, String ldapUrl, String ldapBaseDN) {

    	LDAPUser vLUser = null;
    	
        Hashtable envHash = new Hashtable(1);
        envHash.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CLASS);
        envHash.put(Context.PROVIDER_URL, ldapUrl + "/" + ldapBaseDN);

        DirContext ctx = null;
        try {
        	log.debug("getting LDAPUser - " + userid);
            // Create the initial directory context
            ctx = new InitialDirContext(envHash);
            // Ask for all attributes of the object
            Attributes attrs = ctx.getAttributes("uid=" + userid);

            String fullName = (String) attrs.get("cn").get();
            String email = (String) attrs.get("mail").get();

            vLUser = new LDAPUser(userid, fullName, email);

        } catch (AuthenticationException e) {
            log.error("getLDAPUser: Problem authenticating: " + e);
            e.printStackTrace();
            return null;
        } catch (NamingException e) {
            log.error("getLDAPUser: Problem getting attribute: " + e);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            log.error("getLDAPUser: " + e);
            e.printStackTrace();
            return null;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    // can't do much here...                
                }
            }
        }
        
        return vLUser;
    }
}