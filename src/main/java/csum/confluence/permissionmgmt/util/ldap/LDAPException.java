package csum.confluence.permissionmgmt.util.ldap;

/**
 * Basic Exception wrapper
 * @author Andy Brook
 */
public class LDAPException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6901995466364262665L;

	public LDAPException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public LDAPException(String string) {
        super(string);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public LDAPException(String string, Throwable throwable) {
        super(string, throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public LDAPException(Throwable throwable) {
        super(throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
