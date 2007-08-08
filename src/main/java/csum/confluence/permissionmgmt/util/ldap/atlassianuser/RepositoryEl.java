package csum.confluence.permissionmgmt.util.ldap.atlassianuser;

import java.util.ArrayList;

/** This class models the &lt;repositories&gt; element within the atlassian-user.xml file,
 * its only here to be compatible with the XML structure.
 * 
 * @author Andy Brook
 */
public class RepositoryEl {
	private ArrayList fLdaps=new ArrayList();
	
	public void addLdap(LdapEl r)
	{
		fLdaps.add(r);
	}
	
	public ArrayList getRepositories()
	{
		return fLdaps;
	}
}
