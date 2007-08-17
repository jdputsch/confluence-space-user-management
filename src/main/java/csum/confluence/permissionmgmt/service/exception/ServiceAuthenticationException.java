package csum.confluence.permissionmgmt.service.exception;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 17, 2007
 * Time: 10:48:03 AM
 */
public class ServiceAuthenticationException extends ServiceException {

    public ServiceAuthenticationException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ServiceAuthenticationException(String string) {
        super(string);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ServiceAuthenticationException(String string, Throwable throwable) {
        super(string, throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ServiceAuthenticationException(Throwable throwable) {
        super(throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
