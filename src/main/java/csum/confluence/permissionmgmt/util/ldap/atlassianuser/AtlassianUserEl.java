package csumdevteam.confluence.permissionmgmt.util.ldap.atlassianuser;

import java.util.ArrayList;

/** This class models the &lt;atliasian-user&gt; element within the atlassian-user.xml file,
 * its only here to be compatible with the XML structure.
 * 
 * @author Andy Brook
 */

public class AtlassianUserEl {
	private ArrayList fRepositories=new ArrayList();
	
	public ArrayList getRepositories()
	{
		return fRepositories;
	}
	
	public void addRepositories(RepositoryEl r)
	{
		fRepositories.add(r);
	}
}
