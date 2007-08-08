package csum.confluence.permissionmgmt.util.ldap.atlassianuser;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import csum.confluence.permissionmgmt.util.ldap.ILdapEnvironmentProvider;

/** Test case to provide correct parsing of atlassian-user.xml, also to try a connection (obviously this is going to need a valid atlassian-user.xml file to run on your network)
 * 
 * @author Andy Brook
 */
public class AtlassianUserParserTestCase extends TestCase 
{
	private static final Log LOG = LogFactory.getLog(AtlassianUserParserTestCase.class);
	
	public void testAtlassianUserParser() throws Exception
	{
        // only do this once
        LOG.debug("parsing configuration from osuser.xml LDAP settings");
        try {
        	
            AUParser p = AUParser.getInstance();
            p.parse();
            Hashtable env = ((ILdapEnvironmentProvider)p).getLdapEnvironment();
            assertNotNull("the environment could not be loaded", env);
            
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
            LOG.error("Error in static initializer of OSUserLDAPHelper", e);
            throw e;
        }
	}
}
