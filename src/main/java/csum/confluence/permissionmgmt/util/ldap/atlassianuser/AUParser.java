package csum.confluence.permissionmgmt.util.ldap.atlassianuser;

import csum.confluence.permissionmgmt.util.ldap.ILdapEnvironmentProvider;
import csum.confluence.permissionmgmt.util.ldap.LDAPException;
import csum.confluence.permissionmgmt.util.ldap.LDAPLookup;
import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.naming.Context;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;

/** This class manages the parsing of the atlassian-user.xml file found in WEB-INF/classes of deployed applications
 * 
 * More on the Digester : http://wiki.apache.org/jakarta-commons/Digester
 * @author Andy Brook
 */
public class AUParser implements ILdapEnvironmentProvider{
	private static final Log LOG = LogFactory.getLog(AUParser.class);
	
    private static final String AU_XML_FILENAME="atlassian-user.xml";
    
    public static final String DEFAULT_CONTEXT_FACTORY="com.sun.jndi.ldap.LdapCtxFactory";
    
    private Digester gDigester;
	private AtlassianUserEl fAtlassianUser=null;
	private static AUParser gInstance=null;
		
	private void init()
	{
        gDigester = new Digester();
        gDigester.setValidating( false );       
        gDigester.setErrorHandler(new ErrorHandler(){

			public void error(SAXParseException arg0) throws SAXException {
				LOG.error("error: "+arg0.getLocalizedMessage());
				return;
			}

			public void fatalError(SAXParseException arg0) throws SAXException {
				LOG.fatal("fatal: "+arg0.getLocalizedMessage());
				return;
			}

			public void warning(SAXParseException arg0) throws SAXException {
				LOG.warn("warning: "+arg0.getLocalizedMessage());
				return;
			}});
        gDigester.addObjectCreate( "atlassian-user", AtlassianUserEl.class );        
        gDigester.addObjectCreate( "atlassian-user/repositories", RepositoryEl.class );
        gDigester.addSetNext( "atlassian-user/repositories", "addRepositories" );
        
        gDigester.addObjectCreate( "atlassian-user/repositories/ldap", LdapEl.class );
        gDigester.addSetNext( "atlassian-user/repositories/ldap", "addLdap" );
  
        //deal with properties on ldap tag, mapping attribute keys to bean property names
        gDigester.addSetProperties( "atlassian-user/repositories/ldap", "key", "ldapKey" );
        gDigester.addSetProperties( "atlassian-user/repositories/ldap", "name", "ldapName" );
        gDigester.addSetProperties( "atlassian-user/repositories/ldap", "cache", "ldapCache" );
        
        //deal with sub-elements, mapping to bean property names
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/host", "host" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/port", "port" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/securityPrincipal", "securityPrincipal" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/securityCredential", "securityCredential" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/securityProtocol", "securityProtocol" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/securityAuthentication", "securityAuthentication" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/baseContext", "baseContext" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/baseUserNamespace", "baseUserNamespace" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/baseGroupNamespace", "baseGroupNamespace" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/usernameAttribute", "usernameAttribute" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/userSearchFilter", "userSearchFilter" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/firstnameAttribute", "firstnameAttribute" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/surnameAttribute", "surnameAttribute" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/emailAttribute", "emailAttribute" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/groupnameAttribute", "groupnameAttribute" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/groupSearchFilter", "groupSearchFilter" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/membershipAttribute", "membershipAttribute" );
        
        //options
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/poolingOn", "poolingOn" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/maxSize", "maxSize" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/initSize", "initSize" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/debugLevel", "debugLevel" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/securityProtocol", "securityProtocol" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/authentication", "authentication" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/timeout", "timeout" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/initialContextFactory", "initialContextFactory" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/batchSize", "batchSize" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/timeToLive", "timeToLive" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/userSearchAllDepths", "userSearchAllDepths" );
        gDigester.addBeanPropertySetter( "atlassian-user/repositories/ldap/groupSearchAllDepths", "groupSearchAllDepths" );
	}
	
	protected AUParser()
	{
		init();
	}
	
	public static AUParser getInstance()
	{
		if (gInstance==null)
		{
			gInstance=new AUParser();
		}
		return gInstance;
	}
	
	/** kick off the parser, ensuring the classloader for resource loading
     * is retrieved from the current thread
     * 
	 * @throws IOException
	 * @throws SAXException
	 */
	public void parse() throws IOException,SAXException
	{
		//check we have a SAX parser
		try
		{		
			LOG.debug("checking a SAXParserFactory exists");
			SAXParserFactory.newInstance();
		}
		catch (Exception saxe1)
		{
			saxe1.printStackTrace();
			String msg="No SAX Parser factory present, needs xerces or similar";
			LOG.error(msg);
			throw new SAXException(msg);
		}
		
		//according to http://confluence.atlassian.com/pages/viewpage.action?pageId=200934
		InputStream is = com.atlassian.core.util.ClassLoaderUtils.getResourceAsStream(AU_XML_FILENAME, getClass());
		try
		{			
			if (is!=null)
			{
	        	fAtlassianUser = (AtlassianUserEl)gDigester.parse( is );
	        	LOG.info(AU_XML_FILENAME+" parse completed.");
			}
			else
			{
				String msg="The "+AU_XML_FILENAME+" file could not be found.";
				LOG.error(msg);
				throw new IOException(msg);
			}
		}
		catch (IOException ioe)
		{
			String msg="Problem reading "+AU_XML_FILENAME+" file";
			LOG.error(msg+" - "+ioe.getLocalizedMessage());
			throw ioe;
		}
		catch (SAXException saxe)
		{
			String msg="Unable to parse "+AU_XML_FILENAME+" file";
			LOG.error(msg+" - "+saxe.getLocalizedMessage());
			throw saxe;
		}

		
	}

    /** implement the interface to return the environment, uses the first available LDAP entry in atlassian-user.xml
     * NOTE: scope for specifying ID of the LDAP entry....
     * @return Hashtable
     * @throws LDAPException if it breaks
     */
	public Hashtable getLdapEnvironment() throws LDAPException
	{
		Hashtable env=null;
		if (fAtlassianUser!=null)
		{
			ArrayList aEl = fAtlassianUser.getRepositories();
			if (aEl.size()==0)
			{
				throw new LDAPException("atlassian-user parser hasnt found any repositories element");
			}
			RepositoryEl rEl = (RepositoryEl)aEl.get(0);
			if (rEl.getRepositories().size()==0)
			{
				throw new LDAPException("atlassian-user parser hasnt found any repository elements");
			}
			ArrayList ldapList = rEl.getRepositories();
			LdapEl lEl = (LdapEl)ldapList.get(0);
			
			env=new Hashtable();
			
			//ensure common attributes are present so an InitialContext can be created.
			//eg java.naming.factory.initial and java.naming.provider.url
			setupProviderUrl(env, lEl);			
			setupInitialContextFactory(env,lEl);		
			setupSearchBase(env,lEl);
			setupSecurity(env,lEl);
		}
		else
		{
			String msg=AU_XML_FILENAME+" not loaded, model not found, parse not called?";
			LOG.error(msg);
			throw new LDAPException(msg);
		}
		return env;
	}
	
	/** setup credentials and related keys suitable for an InitialContext creation
	 * 
	 * @param env
	 * @param el
	 */
	private void setupSecurity(Hashtable env, LdapEl el)
	{
		String principal = el.getSecurityPrincipal();
		if (principal!=null)
		{
			env.put(Context.SECURITY_PRINCIPAL, principal);
		}
		
		String credentials = el.getSecurityCredential();
		if (credentials!=null)
		{
			env.put(Context.SECURITY_CREDENTIALS, credentials);
		}
		
		String authentication= el.getSecurityAuthentication();
		if (authentication!=null)
		{
			env.put(Context.SECURITY_AUTHENTICATION, authentication);
		}

		String protocol= el.getSecurityProtocol();
		if (authentication!=null)
		{
			env.put(Context.SECURITY_PROTOCOL, protocol);
		}
	}

	/** setup search base under a common key
	 * 
	 * @param env
	 * @param el
	 */
	private void setupSearchBase(Hashtable env, LdapEl el) throws LDAPException
	{
		String baseContext=el.getBaseContext();
		if (baseContext==null)
		{
			throw new LDAPException("Atlassian User attribute - 'baseContext' NotFound");
		}
		env.put(LDAPLookup.SEARCH_BASE, baseContext);
	}

	/** atlassian user allows for an override, my guess is the default is the SUN factory
	 * 
	 * @param env
	 * @param el
	 */
	private void setupInitialContextFactory(Hashtable env, LdapEl el)
	{
		String initialFactory=(String)env.get("initialContextFactory");
		if (initialFactory==null)
		{
			initialFactory=DEFAULT_CONTEXT_FACTORY;
		}
		env.put(Context.INITIAL_CONTEXT_FACTORY, initialFactory);		
	}

	/** helper method to put together a provider URL derived from other properties
	 * 
	 * @param env
	 * @return
	 * @throws LDAPException
	 */
	private void setupProviderUrl(Hashtable env, LdapEl el) throws LDAPException
	{		
		StringBuffer providerUrl=new StringBuffer();
		
		String protocol=(String)el.getSecurityProtocol();
		if (protocol==null)
		{
			throw new LDAPException("Atlassian User attribute - 'securityProtocol' NotFound");
		}
		
		if (protocol.toLowerCase().indexOf("plain")!=-1)
		{
			providerUrl.append("ldap://");
		}
		else
		{
			providerUrl.append("ldaps://");
		}
		
		String host=(String)el.getHost();
		if (protocol==null)
		{
			throw new LDAPException("Atlassian User attribute - 'host' NotFound");
		}
		else
		{
			providerUrl.append(host.trim());
		}
		
		String port=(String)el.getPort();
		if (port==null)
		{
			throw new LDAPException("Atlassian User attribute - 'port' NotFound");
		}
		else
		{
			providerUrl.append(":"+port.trim());
		}
		
		env.put(Context.PROVIDER_URL, providerUrl.toString());
		
		return;
	}
}
