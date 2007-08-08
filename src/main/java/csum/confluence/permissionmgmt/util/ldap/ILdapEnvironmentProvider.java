package csum.confluence.permissionmgmt.util.ldap;

import java.util.Hashtable;

/** common interface through which a given LDAP connection can be initialised
 * 
 * @author Andy Brook
 */
public interface ILdapEnvironmentProvider
{
	public Hashtable getLdapEnvironment() throws LDAPException;
}
