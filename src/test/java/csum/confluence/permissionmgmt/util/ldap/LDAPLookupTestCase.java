package csum.confluence.permissionmgmt.util.ldap;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Test to prove all the providers can be used to retrieve a valid user from lookup
 * 
 * @author Andy Brook
 *
 */
public class LDAPLookupTestCase extends TestCase
{
	private static final Log LOG = LogFactory.getLog(LDAPLookupTestCase.class);
	
	public void testToTestLDAPWithLDAPServerUncommentLDAPLookupTestCaseTests() {

    }


    // THESE TESTS ARE COMMENTED OUT BECAUSE THEY ONLY WORK IF YOU CAN POINT AT A WORKING LDAP SERVER

    /** Test case to prove osuser parse and lookup
	 * 
	 */
    /*
    public void testOSUserLookup() throws Exception
	{
		Map options=new HashMap();
				
		OSUserParser instance=OSUserParser.getInstance();
		
		//dont forget to parse the content so we can get the initial list of providerClasses
		instance.parse();
		
		//this list to put in the UI somehow if OSUSer provider is required
		List providers = instance.getProviderClasses();
		for (Iterator iterator = providers.iterator(); iterator.hasNext();) {
			String providerClass = (String) iterator.next();
			LOG.info("found provider class: "+providerClass);
			if (providerClass.equalsIgnoreCase("com.opensymphony.user.provider.ldap.LDAPCredentialsProvider"))
			{
				LOG.info("match found, setting required provider");
				//userSelection would be set like this
				instance.setRequiredProvider(providerClass);
				break;
			}
		}
		
		//users would setup the exact attributes containing the relavent information,
		//for AD (my test environment, the following attribute values work
		options.put(LDAPLookup.USERID_ATTRIBUTE_KEY, "sAMAccountName");
		options.put(LDAPLookup.EMAIL_ATTRIBUTE_KEY, "mail");
		options.put(LDAPLookup.NAME_ATTRIBUTE_KEY, "displayName");
		
		LDAPLookup lookup = new LDAPLookup(options);
		//User selection would choose either LDAPLookup.OSUSER_PROVIDER or LDAPLookup.ATLASSIAN_USER_PROVIDER, that value is passed into the constructor
		lookup.setProvider(LDAPLookup.OSUSER_PROVIDER);
		
		//init kicks of the parsing cycle.
		lookup.init();
		
		//actually do the lookup
		LDAPUser u = lookup.getLDAPUser("axb");		
		assertNotNull("LDAP user not found, was null "+u);
		LOG.info("OSUSER source - Got user: id="+u.getUserId()+", name="+u.getFullName()+", email="+u.getEmail());
		return;
	}
    */

    /** Test case to prove atlassian-user parse and lookup
	 * 
	 * @throws Exception
	 */
	/*
    public void testAtlassianUserLookup() throws Exception
	{
		Map options=new HashMap();
		options.put(LDAPLookup.USERID_ATTRIBUTE_KEY, "sAMAccountName");
		options.put(LDAPLookup.EMAIL_ATTRIBUTE_KEY, "mail");
		options.put(LDAPLookup.NAME_ATTRIBUTE_KEY, "displayName");
		
		LDAPLookup lookup = new LDAPLookup(options);
		lookup.setProvider(LDAPLookup.ATLASSIAN_USER_PROVIDER);
		lookup.init();
		
		LDAPUser u = lookup.getLDAPUser("axb");				
		assertNotNull("LDAP user not found, was null "+u);		
		LOG.info("Atlassian User source - Got user: id="+u.getUserId()+", name="+u.getFullName()+", email="+u.getEmail());
		return;		
	}
    */
}
