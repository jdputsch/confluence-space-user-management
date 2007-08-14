package csum.confluence.permissionmgmt.util.ldap.osuser;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import csum.confluence.permissionmgmt.util.ldap.ILdapEnvironmentProvider;

/** Test case to provide correct parsing of osuser.xml, also to try a connection (obviously this is going to need a valid osuser.xml file to run on your network)
 * 
 * @author Andy Brook
 */
public class OSUserParserTestCase extends TestCase 
{
	private static final Log LOG = LogFactory.getLog(OSUserParserTestCase.class);
	
	/** Test the parse completes and a valid environment is retrieved
	 * 
	 * @throws Exception
	 */
	public void testOsUserParser() throws Exception
	{
        // only do this once
        LOG.debug("parsing configuration from osuser.xml LDAP settings");
        try {
            OSUserParser p = OSUserParser.getInstance();
            p.parse();
            List providers = p.getProviderClasses();
            assertNotNull("No providers were found", providers);
            
            //setup the required provider
            p.setRequiredProvider("com.opensymphony.user.provider.ldap.LDAPCredentialsProvider");
                        
            Hashtable env = ((ILdapEnvironmentProvider)p).getLdapEnvironment();	
                        
            assertNotNull("LDAP environement didnt get loaded, its null.",env);

            Set keys = env.keySet();
            for (Iterator iterator = keys.iterator(); iterator.hasNext();)
            {
				String aKey = (String) iterator.next();
				LOG.info("Key '"+aKey+"' = "+env.get(aKey));
			}
            assertTrue("The environment doesnt have an 'java.naming.factory.initial' key set.", env.containsKey("java.naming.factory.initial"));
            assertTrue("The environment doesnt have an 'java.naming.provider.url' key set.", env.containsKey("java.naming.provider.url"));
            
            LOG.debug("example osuser.xml parsed ok");
        }
        catch (Exception e) {
            LOG.error("Error in static initializer", e);
            throw e;
        }
	}
}
