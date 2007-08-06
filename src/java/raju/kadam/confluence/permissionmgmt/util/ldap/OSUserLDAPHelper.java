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

package raju.kadam.confluence.permissionmgmt.util.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Helper class to perform most of the LDAP magic for pulling user details from an LDAP repository extracted from
 * the OSUser xml file.
 *
 * @author Andy Brook
 */
public class OSUserLDAPHelper {
    private static final Log log = LogFactory.getLog(OSUserLDAPHelper.class);

    private static final String OSUSER_XML_FILENAME = "osuser.xml";
    private static final String LDAP_CREDENTIALS_PROVIDER_KEY = "com.opensymphony.user.provider.ldap.LDAPCredentialsProvider";

    private static final String[] REQUIRED_ATTRIBUTES = new String[]{"sAMAccountName", "displayName", "mail"};
    private static final String SEARCH_BASE = "searchBase";

    private String fSearchFilterPrefix = "(&(objectClass=user)(sAMAccountName=";
    private String fSearchBase = null;

    private InitialDirContext fContext = null;
    SearchControls fControl = null;

    /**
     * store for the attributes against the LDAPCredentialsProvider in osuser.xml
     */
    private static Hashtable LDAP_ENV;

    static {
        //only do this once

        //Logger.getRootLogger().setLevel(Level.DEBUG);
        log.debug("initialising NTLM configuration from osuser.xml LDAP settings");
        try {
            OSUserParser p = new OSUserParser();
            InputStream is = p.getClass().getClassLoader().getResourceAsStream(OSUSER_XML_FILENAME);
            p.parse(is);
            LDAP_ENV = p.getProviderPropertiesAsHash(LDAP_CREDENTIALS_PROVIDER_KEY);

            log.debug("completed initialising");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     public static final void main(String[] args) throws Exception
     {
         OSUserLDAPHelper helper = new OSUserLDAPHelper();
         helper.init();
         LDAPUser data = helper.getLDAPUser("RS");
         if (data!=null)
         {
             System.out.println("UserData: "+data.getUserId()+" - "+data.getFullName()+" - "+data.getEmail());
         }
         else
         {
             System.out.println("No user data...");
         }
     }
     */

    /**
     * Initialise environment, the defualt will attempt to load osuser.xml and use the associated environment
     *
     * @throws Exception
     */
    void init() throws Exception {
        log.debug("getting environement");
        Hashtable env = null;

        env = getOSUserEnv();
        //env=getTestEnv();
        init(env);
    }

    /**
     * allow environment to be passed in
     */
    void init(Hashtable env) throws Exception {
        log.debug("getting initial context");
        fContext = initLDAPContext(env);

        fSearchBase = (String) env.get(SEARCH_BASE);

        log.debug("setting controls");
        fControl = new SearchControls();
        fControl.setReturningAttributes(REQUIRED_ATTRIBUTES);
        fControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    /**
     * Initialise the environement required for a Context to be created.
     * If it fails to load that try some locally defined values.
     *
     * @return initialised environment
     */
    public Hashtable getOSUserEnv() {
        Hashtable env = LDAP_ENV;

        if (env == null) {
            log.error("Local osuser.xml file could not be parsed, using locally defined ldap connection details - THIS PROBABLY WONT WORK!");
            throw new IllegalStateException("No environment found, unable to initialise LDAP hookup for new users");
        }
        return env;
    }

//	/** provide a test environment to verify correct operation
//	 *
//	 * @return
//	 */
//	public Hashtable getTestEnv()
//	{
//		Hashtable env = LDAP_ENV;
//		if (env==null)
//		{
//			env = new Hashtable();
//			env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
//			env.put(Context.SECURITY_AUTHENTICATION, LDAP_SIMPLE_AUTH);
//			env.put(Context.SECURITY_PRINCIPAL, "wasadmin@dolby.net"); // needs to have @dolby.net appended
//			env.put(Context.SECURITY_CREDENTIALS, "do+dr4a7");
//			env.put(Context.PROVIDER_URL, "ldap://cuckoo.dolby.net:389");
//			env.put(SEARCH_BASE, "DC=dolby,DC=net");
//		}
//		return env;
//	}


    /**
     * Initialise LDAP Context, make ready for querying
     *
     * @param env
     * @return Initialised Context
     * @throws Exception
     */
    private InitialDirContext initLDAPContext(Hashtable env) throws Exception {
        InitialDirContext context = null;
        try {
            log.debug("Creating InitialDirContext for LDAP server: " + env.get(Context.PROVIDER_URL) + "/" + env.get(SEARCH_BASE));
            context = new InitialDirContext(env);
            log.debug("Context created.");
        }
        catch (NamingException ne) {
            log.error("Unable to configure initial context: " + ne.getLocalizedMessage());
            ne.printStackTrace();

            if (ne.getLocalizedMessage().indexOf("AcceptSecurityContext") == -1) {
                ne.printStackTrace();
                // rethrow, exception not due to authentication...
                throw new Exception("LDAP directory connection (authentication) failed.", ne);
            }
        }

        return context;
    }

    /**
     * Get LDAP user details for the given username
     *
     * @param username
     * @return NTLMAwareAuthenticator.UserData
     * @throws Exception
     */
    public LDAPUser getLDAPUser(String username) throws Exception {
        LDAPUser data = null;

        if (fContext != null) {
            String searchFilter = fSearchFilterPrefix + username.trim() + "))";
            log.debug("Searching for user based on filter: [" + searchFilter + "]");
            NamingEnumeration matchingEnum = null;
            try {
                matchingEnum = fContext.search(fSearchBase, searchFilter, null, fControl);
                data = processEnum(searchFilter, matchingEnum);
            }
            catch (CommunicationException ce) {
                log.debug("Active Directory Session appears to have been timed out, trying to revalidate");
                //revalidate if AD session has timed out

                Hashtable env = getOSUserEnv();
                fContext = initLDAPContext(env);

                try {
                    matchingEnum = fContext.search(fSearchBase, searchFilter, null, fControl);
                    data = processEnum(searchFilter, matchingEnum);
                }
                catch (NamingException ne) {
                    ne.printStackTrace();
                    log.error("Unable to execute search after attempted session timeout revlidation, " + ne.getLocalizedMessage());
                    throw new Exception("Unable to execute search after attempted session timeout revlidation", ne);
                }
            }
            catch (NamingException ne) {
                ne.printStackTrace();
                log.error("Unable to execute search, " + ne.getLocalizedMessage());
                throw new Exception("Unable to execute search", ne);
            }
        } else {
            throw new Exception("No context.");
        }

        return data;
    }

    /**
     * Process the returned enumeration, picking out the attributes we care about,
     * ignoring mulitple hits.
     *
     * @param en
     * @return
     * @throws Exception
     */
    private LDAPUser processEnum(String searchFilter, NamingEnumeration en) throws Exception {
        LDAPUser data = null;
        if (en != null && en.hasMoreElements()) {
            data = new LDAPUser();

            //only expect 1 record, ignore remainder
            SearchResult sr = (SearchResult) en.nextElement();
            Attributes attribs = sr.getAttributes();
            NamingEnumeration attribIDs = attribs.getIDs();
            while (attribIDs.hasMoreElements()) {
                String anAttribKey = (String) attribIDs.nextElement();
                for (int idx = 0; idx < REQUIRED_ATTRIBUTES.length; idx++) {
                    String anAttrib = REQUIRED_ATTRIBUTES[idx];
                    if (anAttribKey.equalsIgnoreCase(anAttrib)) {
                        Attribute att = attribs.get(anAttribKey);
                        try {
                            String value = (String) att.get(0); // properties we want arent lists
                            switch (idx) {
                                case 0:
                                    data.setUserId(value.toLowerCase());
                                    break;
                                case 1:
                                    data.setFullName(value);
                                    break;
                                case 2:
                                    data.setEmail(value);
                                    break;
                                default:
                                    throw new Exception("Unknown index, fix code!");
                            }
                            ;
                        }
                        catch (NamingException ne) {
                            ne.printStackTrace();
                            log.error("Unable to retrieve attributes for key " + anAttribKey + ", " + ne.getLocalizedMessage());
                            throw new Exception("Unable to retrieve attributes for key " + anAttribKey, ne);
                        }
                    }//all attributes we want
                }//all attributes found
			}//records found
		}//enumeration valid
		return data;
	}
}
