package csum.confluence.permissionmgmt.util.ldap;

import csum.confluence.permissionmgmt.config.CustomPermissionConfiguration;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigurable;
import csum.confluence.permissionmgmt.util.ldap.osuser.OSUserParser;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 9, 2007
 * Time: 1:10:45 PM
 */
public class LDAPHelper {

    private static final Log log = LogFactory.getLog(LDAPHelper.class);

    public static LDAPUser getLDAPUser(CustomPermissionConfigurable config, String userid) throws ParserConfigurationException, LDAPException {
        Map options = new HashMap();

        //users would setup the exact attributes containing the relavent information,
        //for AD (my test environment, the following attribute values work
        options.put(LDAPLookup.USERID_ATTRIBUTE_KEY, config.getLdapUserIdAttribute());
        options.put(LDAPLookup.EMAIL_ATTRIBUTE_KEY, config.getLdapEmailAttribute());
        options.put(LDAPLookup.FIRSTNAME_ATTRIBUTE_KEY, config.getLdapFirstNameAttribute());
        options.put(LDAPLookup.LASTNAME_ATTRIBUTE_KEY, config.getLdapLastNameAttribute());

        LDAPLookup lookup = new LDAPLookup(options);
        //User selection would choose either LDAPLookup.OSUSER_PROVIDER or LDAPLookup.ATLASSIAN_USER_PROVIDER, that value is passed into the constructor

        String providerType = config.getProviderType();
        if (CustomPermissionConfigConstants.PROVIDER_TYPE_ATLASSIAN_USER.equals(providerType)) {
            log.debug("Using atlassian-user as provider type for LDAP config. Provider type from config was " + providerType);
            lookup.setProvider(LDAPLookup.ATLASSIAN_USER_PROVIDER);
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

            lookup.setProvider(LDAPLookup.OSUSER_PROVIDER);
        }

        //init kicks of the parsing cycle.
        lookup.init();

        //actually do the lookup
        LDAPUser u = lookup.getLDAPUser(userid);
        return u;
    }
}
