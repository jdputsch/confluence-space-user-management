package csum.confluence.permissionmgmt.util.ldap.atlassianuser;


/** This class models the &lt;ldap&gt; element within the atlassian-user.xml file,
 * not all sub-element values need to be present, they will be left as nulls here.
 * 
 * @author Andy Brook
 */
public class LdapEl
{
	//ldap attributes	
	private String fLdapKey;
	private String fLdapName;
	private String fLdapCache;

	//properties	
	private String fHost;
	private String fPort;
	private String securityPrincipal;
    private String securityCredential;
    private String securityProtocol;
    private String securityAuthentication;
    private String baseContext;
	private String baseUserNamespace;
    private String baseGroupNamespace;
    private String usernameAttribute;
    private String userSearchFilter;
    private String firstnameAttribute;
    private String surnameAttribute;
    private String emailAttribute;
    private String groupnameAttribute;
    private String groupSearchFilter;
    private String membershipAttribute;
    
    //options    
	private String poolingOn;
	private String maxSize;
	private String initSize;
	private String prefSize;
	private String debugLevel;
	private String authentication;
	private String timeout;
	private String initialContextFactory;
	private String batchSize;
	private String timeToLive;
	private String userSearchAllDepths;
	private String groupSearchAllDepths;
	
	public LdapEl(){}
	
	public String getHost() {
		return fHost;
	}

	public void setHost(String host) {
		fHost = host;		
	}

	public String getLdapKey() {
		return fLdapKey;
	}

	public void setLdapKey(String ldapKey) {
		fLdapKey = ldapKey;
	}

	public String getLdapName() {
		return fLdapName;
	}

	public void setLdapName(String ldapName) {
		fLdapName = ldapName;
	}

	public String getLdapCache() {
		return fLdapCache;
	}

	public void setLdapCache(String ldapCache) {
		fLdapCache = ldapCache;
	}

	public String getSecurityPrincipal() {
		return securityPrincipal;
	}

	public void setSecurityPrincipal(String securityPrincipal) {
		this.securityPrincipal = securityPrincipal;
	}

	public String getSecurityCredential() {
		return securityCredential;
	}

	public void setSecurityCredential(String securityCredential) {
		this.securityCredential = securityCredential;
	}
	
	public String getSecurityAuthentication() {
		return securityAuthentication;
	}

	public void setSecurityAuthentication(String securityAuthentication) {
		this.securityAuthentication = securityAuthentication;
	}

	public String getBaseContext() {
		return baseContext;
	}

	public void setBaseContext(String baseContext) {
		this.baseContext = baseContext;
	}

	public String getBaseUserNamespace() {
		return baseUserNamespace;
	}

	public void setBaseUserNamespace(String baseUserNamespace) {
		this.baseUserNamespace = baseUserNamespace;
	}

	public String getBaseGroupNamespace() {
		return baseGroupNamespace;
	}

	public void setBaseGroupNamespace(String baseGroupNamespace) {
		this.baseGroupNamespace = baseGroupNamespace;
	}

	public String getUsernameAttribute() {
		return usernameAttribute;
	}

	public void setUsernameAttribute(String usernameAttribute) {
		this.usernameAttribute = usernameAttribute;
	}

	public String getUserSearchFilter() {
		return userSearchFilter;
	}

	public void setUserSearchFilter(String userSearchFilter) {
		this.userSearchFilter = userSearchFilter;
	}

	public String getFirstnameAttribute() {
		return firstnameAttribute;
	}

	public void setFirstnameAttribute(String firstnameAttribute) {
		this.firstnameAttribute = firstnameAttribute;
	}

	public String getSurnameAttribute() {
		return surnameAttribute;
	}

	public void setSurnameAttribute(String surnameAttribute) {
		this.surnameAttribute = surnameAttribute;
	}

	public String getEmailAttribute() {
		return emailAttribute;
	}

	public void setEmailAttribute(String emailAttribute) {
		this.emailAttribute = emailAttribute;
	}

	public String getGroupnameAttribute() {
		return groupnameAttribute;
	}

	public void setGroupnameAttribute(String groupnameAttribute) {
		this.groupnameAttribute = groupnameAttribute;
	}

	public String getGroupSearchFilter() {
		return groupSearchFilter;
	}

	public void setGroupSearchFilter(String groupSearchFilter) {
		this.groupSearchFilter = groupSearchFilter;
	}

	public String getMembershipAttribute() {
		return membershipAttribute;
	}

	public void setMembershipAttribute(String membershipAttribute) {
		this.membershipAttribute = membershipAttribute;
	}

	public String getPoolingOn() {
		return poolingOn;
	}

	public void setPoolingOn(String poolingOn) {
		this.poolingOn = poolingOn;
	}

	public String getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(String maxSize) {
		this.maxSize = maxSize;
	}

	public String getInitSize() {
		return initSize;
	}

	public void setInitSize(String initSize) {
		this.initSize = initSize;
	}

	public String getPrefSize() {
		return prefSize;
	}

	public void setPrefSize(String prefSize) {
		this.prefSize = prefSize;
	}

	public String getDebugLevel() {
		return debugLevel;
	}

	public void setDebugLevel(String debugLevel) {
		this.debugLevel = debugLevel;
	}

	public String getSecurityProtocol() {
		return securityProtocol;
	}

	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol = securityProtocol;
	}

	public String getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getInitialContextFactory() {
		return initialContextFactory;
	}

	public void setInitialContextFactory(String initialContextFactory) {
		this.initialContextFactory = initialContextFactory;
	}

	public String getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(String batchSize) {
		this.batchSize = batchSize;
	}

	public String getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(String timeToLive) {
		this.timeToLive = timeToLive;
	}

	public String getUserSearchAllDepths() {
		return userSearchAllDepths;
	}

	public void setUserSearchAllDepths(String userSearchAllDepths) {
		this.userSearchAllDepths = userSearchAllDepths;
	}

	public String getGroupSearchAllDepths() {
		return groupSearchAllDepths;
	}

	public void setGroupSearchAllDepths(String groupSearchAllDepths) {
		this.groupSearchAllDepths = groupSearchAllDepths;
	}

	public String getPort() {
		return fPort;
	}

	public void setPort(String port) {
		fPort = port;
	}
}
