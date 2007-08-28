package csum.confluence.permissionmgmt.util.ldap;

import csum.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigurable;
import com.dolby.confluence.net.ldap.osuser.OSUserParser;
import com.dolby.confluence.net.ldap.LDAPUser;
import com.dolby.confluence.net.ldap.LDAPException;
import com.dolby.confluence.net.ldap.LDAPLookupUtil;
import com.dolby.confluence.net.ldap.ILdapEnvironmentProvider;
import com.dolby.confluence.net.ldap.atlassianuser.AUParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Hashtable;
import java.util.Properties;
import java.io.IOException;

/**
 * (c) 2007 Duke University User: gary.weaver@duke.edu Date: Aug 9, 2007 Time: 1:10:45 PM
 */
public class LDAPHelper
{

	private static final Log log = LogFactory.getLog(LDAPHelper.class);

	public static LDAPUser getLDAPUser(CustomPermissionConfigurable config, String userid) throws ParserConfigurationException,
			LDAPException
	{		
		Properties builtConfig=buildConfig(config);
		LDAPLookupUtil lookup = new LDAPLookupUtil(builtConfig);
		LDAPUser u = lookup.getUserDetails(userid);
		return u;
	}
	
	private static Properties buildConfig(CustomPermissionConfigurable config)
	{
		Properties p=new Properties();

		p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_ID_ATTRIBUTE, config.getLdapUserIdAttribute());
		p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_EMAIL_ATTRIBUTE, config.getLdapEmailAttribute());
		p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_FIRST_NAME_ATTRIBUTE, config.getLdapFirstNameAttribute());
		p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_LAST_NAME_ATTRIBUTE, config.getLdapLastNameAttribute());
		p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NARROWING_FILTER_EXPRESSION, "(objectclass=user)");

		String userFullNameFormat = config.getUserFullNameFormat();
		boolean lastCommaFirstFormat = (userFullNameFormat != null) && (userFullNameFormat.equals(CustomPermissionConfigConstants.USER_FULL_NAME_FORMAT_TYPE_LASTNAME_COMMA_FIRSTNAME));
		boolean firstLastFormat = (userFullNameFormat != null) && (userFullNameFormat.equals(CustomPermissionConfigConstants.USER_FULL_NAME_FORMAT_TYPE_FIRSTNAME_LASTNAME));
		boolean idFormat = (userFullNameFormat != null) && (userFullNameFormat.equals(CustomPermissionConfigConstants.USER_FULL_NAME_FORMAT_TYPE_ID));

		if (lastCommaFirstFormat)
		{
			p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NAME_FORMAT, "0");
		}
		else if (firstLastFormat)
		{
			p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NAME_FORMAT, "1");
		}
		else if (idFormat)
		{
			p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NAME_FORMAT, "2");
		}
		else
		{
			log.warn("Unknown user fullname format '" + userFullNameFormat + "'. Setting fullname format to 2");
			p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NAME_FORMAT, "2");
		}

		p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_FORCE_USERID_CASE, "2");
		p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_CONTROL_SUBTREE_SCOPE, "2");

		// User selection would choose either LDAPLookup.OSUSER_PROVIDER or LDAPLookup.ATLASSIAN_USER_PROVIDER
		String providerType = config.getProviderType();
		if (CustomPermissionConfigConstants.PROVIDER_TYPE_ATLASSIAN_USER.equals(providerType))
		{
			log.debug("Using atlassian-user as provider type for LDAP config. Provider type from config was " + providerType);
			p.setProperty(LDAPLookupUtil.LDAPUTIL_PROVIDER_TYPE, LDAPLookupUtil.LDAPUTIL_PROVIDER_ATLASSIAN_USER);
		}
		else
		{
			log.debug("Using osuser as provider type for LDAP config since it didn't match " +
					CustomPermissionConfigConstants.PROVIDER_TYPE_ATLASSIAN_USER + ". Provider type from config was " +
					providerType);
			p.setProperty(LDAPLookupUtil.LDAPUTIL_PROVIDER_TYPE, LDAPLookupUtil.LDAPUTIL_PROVIDER_OSUSER);
			p.setProperty(LDAPLookupUtil.LDAPUTIL_OSUSER_PROVIDERCLASS, config.getLdapProviderFullyQualifiedClassname());
		}
		
		if (log.isInfoEnabled())
		{
			Enumeration e = p.keys();
			do
			{
				String key=(String)e.nextElement();
				log.info("LDAP Env: "+key+" = "+p.getProperty(key));
			} while (e.hasMoreElements());
		}

		return p;
	}
}
