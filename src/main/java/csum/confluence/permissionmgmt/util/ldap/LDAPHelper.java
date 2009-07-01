/**
 * Copyright (c) 2007-2009, Custom Space User Management Plugin Development Team
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
 *     * Neither the name of the Custom Space User Management Plugin Development Team
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

package csum.confluence.permissionmgmt.util.ldap;

import com.dolby.confluence.net.ldap.LDAPException;
import com.dolby.confluence.net.ldap.LDAPLookupUtil;
import com.dolby.confluence.net.ldap.LDAPUser;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigConstants;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigurable;
import csum.confluence.permissionmgmt.util.logging.LogUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Gary S. Weaver
 */
public class LDAPHelper {
    private static final Log log = LogFactory.getLog(LDAPHelper.class);

    public static LDAPUser getLDAPUser(CustomPermissionConfigurable config, String userid) throws ParserConfigurationException,
            LDAPException {
        Properties builtConfig = buildConfig(config, userid);
        LDAPUser u = null;

        // this lock is on a static object (this class, not the instance) so that it is JVM-wide
        // error this is intended to fix: "SAX problem: FWK005 parse may not be called while parsing."
        // error case: http://developer.atlassian.com/jira/browse/SUSR-58
        // TODO: ldaputil or the libraries it uses need to be fixed to avoid locking like this
        synchronized (LDAPHelper.class) {
            LDAPLookupUtil lookup = new LDAPLookupUtil(builtConfig);
            u = lookup.getUserDetails(userid);
        }
        return u;
    }

    private static Properties buildConfig(CustomPermissionConfigurable config, String userid) {
        Properties p = new Properties();

        String narrowingFilterExpression = config.getLdapNarrowingFilterExpression();
        if (narrowingFilterExpression != null && !"".equals(narrowingFilterExpression.trim())) {
            // example "(objectclass=user)"
            p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NARROWING_FILTER_EXPRESSION, narrowingFilterExpression);
        }

        String userFullNameFormat = config.getUserFullNameFormat();
        boolean lastCommaFirstFormat = (userFullNameFormat != null) && (userFullNameFormat.equals(CustomPermissionConfigConstants.USER_FULL_NAME_FORMAT_TYPE_LASTNAME_COMMA_FIRSTNAME));
        boolean firstLastFormat = (userFullNameFormat != null) && (userFullNameFormat.equals(CustomPermissionConfigConstants.USER_FULL_NAME_FORMAT_TYPE_FIRSTNAME_LASTNAME));
        boolean idFormat = (userFullNameFormat != null) && (userFullNameFormat.equals(CustomPermissionConfigConstants.USER_FULL_NAME_FORMAT_TYPE_ID));

        if (lastCommaFirstFormat) {
            p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NAME_FORMAT, "0");
        } else if (firstLastFormat) {
            p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NAME_FORMAT, "1");
        } else if (idFormat) {
            p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NAME_FORMAT, "2");
        } else {
            LogUtil.warnWithRemoteUserInfo(log, "Unknown user fullname format '" + userFullNameFormat + "'. Setting fullname format to 2");
            p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_NAME_FORMAT, "2");
        }

        // make all userids lowercase (the Confluence API seems to enforce lowercase for usernames and groupnames)
        p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_FORCE_USERID_CASE, "0");
        // no need yet to make this configurable
        p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_CONTROL_SUBTREE_SCOPE, "2");

        // User selection would choose either LDAPLookup.OSUSER_PROVIDER or LDAPLookup.ATLASSIAN_USER_PROVIDER
        String providerType = config.getProviderType();
        if (CustomPermissionConfigConstants.PROVIDER_TYPE_ATLASSIAN_USER.equals(providerType)) {
            log.debug("Using atlassian-user as provider type for LDAP config. Provider type from config was " + providerType);
            p.setProperty(LDAPLookupUtil.LDAPUTIL_PROVIDER_TYPE, LDAPLookupUtil.LDAPUTIL_PROVIDER_ATLASSIAN_USER);
        } else {
            log.debug("Using osuser as provider type for LDAP config since it didn't match " +
                    CustomPermissionConfigConstants.PROVIDER_TYPE_ATLASSIAN_USER + ". Provider type from config was " +
                    providerType);
            p.setProperty(LDAPLookupUtil.LDAPUTIL_PROVIDER_TYPE, LDAPLookupUtil.LDAPUTIL_PROVIDER_OSUSER);
            p.setProperty(LDAPLookupUtil.LDAPUTIL_OSUSER_PROVIDERCLASS, config.getLdapProviderFullyQualifiedClassname());
            p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_ID_ATTRIBUTE, config.getLdapUserIdAttribute());
            p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_EMAIL_ATTRIBUTE, config.getLdapEmailAttribute());
            p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_FIRST_NAME_ATTRIBUTE, config.getLdapFirstNameAttribute());
            p.setProperty(LDAPLookupUtil.LDAPUTIL_LDAP_LAST_NAME_ATTRIBUTE, config.getLdapLastNameAttribute());
        }

        p.setProperty(LDAPLookupUtil.LDAPUTIL_INITIAL_CONTEXT_VALIDATION_LOOKUP_USER, userid);

        if (log.isInfoEnabled()) {
            Enumeration e = p.keys();
            do {
                String key = (String) e.nextElement();
                log.info("LDAP Env: " + key + " = " + p.getProperty(key));
            } while (e.hasMoreElements());
        }

        return p;
    }
}
