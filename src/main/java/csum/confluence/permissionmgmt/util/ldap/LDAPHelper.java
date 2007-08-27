package csum.confluence.permissionmgmt.util.ldap;

import csum.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigurable;
import com.dolby.confluence.net.ldap.osuser.OSUserParser;
import com.dolby.confluence.net.ldap.LDAPUser;
import com.dolby.confluence.net.ldap.LDAPException;
import com.dolby.confluence.net.ldap.LDAPLookupUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Iterator;
import java.util.List;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 9, 2007
 * Time: 1:10:45 PM
 */
public class LDAPHelper {

    private static final Log log = LogFactory.getLog(LDAPHelper.class);

    public static LDAPUser getLDAPUser(CustomPermissionConfigurable config, String userid) throws ParserConfigurationException, LDAPException {

        LDAPLookupUtil lookup = new LDAPLookupUtil();

        //users would setup the exact attributes containing the relavent information,
        //for AD (my test environment, the following attribute values work

        //http://svn.atlassian.com/svn/public/contrib/confluence/libraries/ldaputil/tags/1.0/src/main/resources/ldaputil.properties
        lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_LDAP_SEARCH_BASE, "REQUIRED");
        lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_LDAP_ID_ATTRIBUTE, config.getLdapUserIdAttribute());
        lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_LDAP_EMAIL_ATTRIBUTE, config.getLdapEmailAttribute());
        lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_LDAP_FIRST_NAME_ATTRIBUTE, config.getLdapFirstNameAttribute());
        lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_LDAP_LAST_NAME_ATTRIBUTE, config.getLdapLastNameAttribute());
        lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NARROWING_FILTER_EXPRESSION, "(objectclass=user)");
        lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NAME_FORMAT, "0");
        lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_LDAP_FORCE_USERID_CASE, "2");
        lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_LDAP_CONTROL_SUBTREE_SCOPE, "2");

        //User selection would choose either LDAPLookup.OSUSER_PROVIDER or LDAPLookup.ATLASSIAN_USER_PROVIDER, that value is passed into the constructor

        String providerType = config.getProviderType();
        if (CustomPermissionConfigConstants.PROVIDER_TYPE_ATLASSIAN_USER.equals(providerType)) {
            log.debug("Using atlassian-user as provider type for LDAP config. Provider type from config was " + providerType);
            lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_PROVIDER_TYPE, LDAPLookupUtil.LDAPUTIL_PROVIDER_ATLASSIAN_USER);
        } else {
            log.debug("Using osuser as provider type for LDAP config since it didn't match " +
                    CustomPermissionConfigConstants.PROVIDER_TYPE_ATLASSIAN_USER +
                    ". Provider type from config was " + providerType);

            OSUserParser instance = OSUserParser.getInstance();

            //dont forget to parse the content so we can get the initial list of providerClasses
            instance.parse();

            //this list to put in the UI somehow if OSUSer provider is required
            List providers = instance.getProviderClasses();
            for (Iterator iterator = providers.iterator(); iterator.hasNext();) {
                String providerClass = (String) iterator.next();
                log.info("found provider class: " + providerClass);
                if (providerClass.equalsIgnoreCase(config.getLdapProviderFullyQualifiedClassname())) {
                    log.info("match found, setting required provider");
                    //userSelection would be set like this
                    instance.setRequiredProvider(providerClass);
                    break;
                }
            }

            lookup.setLDAPEnvProperty(LDAPLookupUtil.LDAPUTIL_PROVIDER_TYPE, LDAPLookupUtil.LDAPUTIL_PROVIDER_OSUSER);
        }

        //actually do the lookup
        LDAPUser u = lookup.getUserDetails(userid);
        return u;
    }
}
