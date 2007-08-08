package csum.confluence.permissionmgmt.util.ldap;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import csum.confluence.permissionmgmt.util.ldap.atlassianuser.AUParser;
import csum.confluence.permissionmgmt.util.ldap.osuser.OSUserParser;


/** Common ldap accessor class, relies on other sources for ldap environment.
 * 
 * Expected Usage:
 * 
 * UI setup, a list of provides in kept in a simple array under PROVIDER_NAMES,
 * only OSUSER_PROVIDER requires some special handling.
 * 
 * <h3>OSUSER_PROVIDER handing</h3>
 * in order to use the osuser parser, a <code>providerClass</code> needs to be specified.  In order to list the available
 * osuser providerClasses, do this:
 * <pre>
 *  OSUserParser oup=OSUserParser.getInstance();
 *  List providerClassNames = oup.getProviderClasses();
 *  
 * </pre>
 * The user needs to choose which to use, and cause this to be recorded via a call to OSUSerParser.getInstance().setRequiredProvider(String provider)
 * 
 * <h3>LDAPLookup Initialisation</h3>
 * When creating a new LDAPLookup object, a map of attributes is required, this will identify the following:
 * <UL>
 * <LI>USERID_ATTRIBUTE_KEY
 * <LI>NAME_ATTRIBUTE_KEY
 * <LI>EMAIL_ATTRIBUTE_KEY
 * </UL>
 * These attributes will form part of the query to the LDAP repository to (a) lookup the user record via USERID_ATTRIBUTE_KEY and to return
 * the name and email also.
 * 
 * The associated test cases demonstrate parser funtion and LDAP connection/retrieval.  
 * 
 * @author Andy Brook
 */
public class LDAPLookup
{
	private static final Log LOG = LogFactory.getLog(LDAPLookup.class);
	
    public static final String SEARCH_BASE = "searchBase";

    private InitialDirContext initialDirContext;
    private SearchControls searchControls;

    /** indicates selection */
	private int fProviderType=-1;

    /** expose provides for selection */
    public static final int ATLASSIAN_USER_PROVIDER=0;
    public static final int OSUSER_PROVIDER=1;

    //indexed to match the keys for ATLASSIAN_USER_PROVIDER and OSUSER_PROVIDER
    public static final String[] PROVIDER_NAMES={"atlassian-user", "osuser"};
    
    /** osuser option keys */
   //static public final String PROVIDER_CLASS="providerClass";
    
    /** common ldap lookup keys for userid, email, name */ 
    static public final String USERID_ATTRIBUTE_KEY="useridAttributeKey";
    static public final String NAME_ATTRIBUTE_KEY="nameAttributeKey";
    static public final String EMAIL_ATTRIBUTE_KEY="emailAttributeKey";

	/**  map containing UI options relavent to the provider */
	private Map fOptions;

	/** Hashtable containing environment required for InitialContext creation */
	private Hashtable fEnv;
        
	/** will store the attributes names that are specific to the ldap source for userid, name, email */
	String[] fRequiredAttributes=null;
	
    /** constructor, provider options in a map to be extracted depending on the provider chosen
     * 
     * @param options
     */
    public LDAPLookup(Map options)    
    {
    	fOptions=options;
    }
    
    /** Method to set the LDAP environment provider, choices include:
     * 'atlassian-user', 'osuser
     * @param providerType
     * @throws IllegalArgumentException if an illegal value is specified
     */
    public void setProvider(int type) throws IllegalArgumentException
    {
    	boolean found=false;
    	switch (type)
    	{
    		case ATLASSIAN_USER_PROVIDER:
    			fProviderType=type;
    			break;
    		case OSUSER_PROVIDER:
    			fProviderType=type;
    			break;
    		default:
    			throw new IllegalArgumentException("Unknown LDAP Provider selected: "+type);
    	};
    }
    
    /**
     * Get LDAP user details for the given username
     *
     * @param username
     * @return NTLMAwareAuthenticator.UserData
     * @throws Exception
     */
    public LDAPUser getLDAPUser(String username) throws LDAPException {
        LDAPUser data = null;

        if (fEnv == null)
        {
        	String msg="Environment not set.  The LDAP providers have not been set and/or initialised.";
        	LOG.error(msg);
        	throw new LDAPException(msg);
        }
        

        if (initialDirContext != null) {
        	String searchBase=(String)fEnv.get(SEARCH_BASE);
        	
            String searchFilter = buildSearchFilter(username);
            
            LOG.debug("Searching for user based on filter: [" + searchFilter + "]");
            NamingEnumeration matchingEnum = null;
            try {
                matchingEnum = initialDirContext.search(searchBase, searchFilter, null, searchControls);
                data = processEnum(searchFilter, matchingEnum);
            }
            catch (CommunicationException ce) {
                LOG.debug("Active Directory Session appears to have been timed out, trying to revalidate", ce);
                //revalidate if AD session has timed out

                initialDirContext = initLDAPContext(fEnv);

                try {
                    matchingEnum = initialDirContext.search(searchBase, searchFilter, null, searchControls);
                    data = processEnum(searchFilter, matchingEnum);
                }
                catch (NamingException ne) {
                    LOG.error("Unable to execute search after attempted session timeout revlidation, " + ne.getLocalizedMessage(), ne);
                    throw new LDAPException("Unable to execute search after attempted session timeout revlidation", ne);
                }
            }
            catch (NamingException ne) {
                LOG.error("Unable to execute search, " + ne.getLocalizedMessage(), ne);
                throw new LDAPException("Unable to execute search", ne);
            }
        }
        else
        {
            throw new LDAPException("initialDirContext was null");
        }

        return data;
    }
        
    /** build the search filter for the given userid based on the attributes set in the options
     * 
     * @param id
     * @return
     */
    private String buildSearchFilter(String id) {
    	String userId=id.trim();
    	StringBuffer filter=new StringBuffer();
    	filter.append("(");
    	String userIdKey=(String)fOptions.get(LDAPLookup.USERID_ATTRIBUTE_KEY);
    	filter.append(userIdKey.trim());
    	filter.append("=");
    	filter.append(userId);
    	filter.append(")");
		return filter.toString();
	}

	/**
     * allow environment to be passed in
     */
    public void init() throws LDAPException {
    	fEnv=new Hashtable();
    	
        LOG.debug("Initialising LDAP environement.");
        
        if (fProviderType==-1)
        {
        	throw new LDAPException("No provider type has been set.");
        }
        
        //valid provider must therefore exist
        switch (fProviderType)
        {
        	case OSUSER_PROVIDER:
                OSUserParser oup = OSUserParser.getInstance();
                
                try
                {
                	oup.parse();
                }
                catch (ParserConfigurationException pce)
                {
                	String msg="SAX Parse failed for OS-User ldap provider";
                	LOG.error(msg+" - "+pce.getLocalizedMessage());
                	throw new LDAPException(msg, pce);
                }
                           
                fEnv = ((ILdapEnvironmentProvider)oup).getLdapEnvironment();	        		
        		break;
        		
        	case ATLASSIAN_USER_PROVIDER:
                AUParser aup = AUParser.getInstance();
                try {
					aup.parse();
				} catch (IOException ioe) {
                	String msg="IO problem for for Atlassian-User ldap provider";
                	LOG.error(msg+" - "+ioe.getLocalizedMessage());
                	throw new LDAPException(msg, ioe);
				} catch (SAXException saxe) {
                	String msg="SAX problem for for Atlassian-User ldap provider";
                	LOG.error(msg+" - "+saxe.getLocalizedMessage());
                	throw new LDAPException(msg, saxe);
				}
                fEnv = ((ILdapEnvironmentProvider)aup).getLdapEnvironment();        		
        		break;
        		
        	default:
        		//shouldnt happen
        		throw new LDAPException("huh?");
        }
        
        //sanity check
        if (fEnv==null)
        {
        	String msg="Sanity check failed, LDAP environment wasnt loaded by any means.";
        	LOG.error(msg);
        	throw new LDAPException(msg);
        }
        else
        {
        	boolean hasInitialFactory=fEnv.containsKey("java.naming.factory.initial");
        	boolean hasProviderURL=fEnv.containsKey("java.naming.provider.url");
        	if (!hasInitialFactory || !hasInitialFactory)
        	{
        		String msg="key fields are not present in the LDAP environment (java.naming.factory.initial="+hasInitialFactory+", java.naming.provider.url="+hasProviderURL+") , its not going to work.";
        	}
        }
        

        //setup initial context, ready for querying
	    initialDirContext = initLDAPContext(fEnv);
	
	    //setup required fields we want back, userId, name, mail.	
	    String useridAttKey=(String)fOptions.get(USERID_ATTRIBUTE_KEY);
	    String nameAttKey=(String)fOptions.get(NAME_ATTRIBUTE_KEY);
	    String emailAttKey=(String)fOptions.get(EMAIL_ATTRIBUTE_KEY);
	    fRequiredAttributes=new String[]{useridAttKey, nameAttKey, emailAttKey};
	    
	    LOG.debug("setting controls");
	    searchControls = new SearchControls();	    
	    searchControls.setReturningAttributes(fRequiredAttributes);
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);	    
	    
	    //ready for getLDAPUser now
	    return;
    }
    
    /**
     * Initialise LDAP Context, make ready for querying
     *
     * @param env
     * @return Initialised Context
     * @throws Exception
     */
    private InitialDirContext initLDAPContext(Hashtable env) throws LDAPException {
        InitialDirContext context = null;
        try {
            LOG.debug("Creating InitialDirContext for LDAP server: " + env.get(Context.PROVIDER_URL) );
            context = new InitialDirContext(env);
            LOG.debug("Context created.");
        }
        catch (NamingException ne) {
            LOG.error("Unable to configure initial context: " + ne.getLocalizedMessage(), ne);

            if (ne.getLocalizedMessage().indexOf("AcceptSecurityContext") == -1) {
                // rethrow, exception not due to authentication...
                throw new LDAPException("LDAP directory connection (authentication) failed.", ne);
            }
        }

        return context;
    }
    
    /**
     * Process the returned enumeration, picking out the attributes we care about,
     * ignoring mulitple hits.
     *
     * @param en
     * @return
     * @throws Exception
     */
    private LDAPUser processEnum(String searchFilter, NamingEnumeration en) throws LDAPException {
        LDAPUser data = null;
        if (en != null && en.hasMoreElements()) {
            data = new LDAPUser();

            //only expect 1 record, ignore remainder
            SearchResult sr = (SearchResult) en.nextElement();
            Attributes attribs = sr.getAttributes();
            NamingEnumeration attribIDs = attribs.getIDs();
            while (attribIDs.hasMoreElements()) {
                String anAttribKey = (String) attribIDs.nextElement();
                
                for (int idx = 0; idx < fRequiredAttributes.length; idx++) {
                    String anAttrib = fRequiredAttributes[idx];
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
                                    throw new LDAPException("Unknown index, fix code!");
                            }
                            ;
                        }
                        catch (NamingException ne)
                        {
                            LOG.error("Unable to retrieve attributes for key " + anAttribKey + ", " + ne.getLocalizedMessage(), ne);
                            throw new LDAPException("Unable to retrieve attributes for key " + anAttribKey, ne);
                        }
                    }//all attributes we want
                }//all attributes found
			}//records found
		}//enumeration valid
		return data;
	}

}
