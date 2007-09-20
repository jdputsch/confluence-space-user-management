package csum.confluence.permissionmgmt.service.exception;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Sep 20, 2007
 * Time: 9:47:59 AM
 */
public class UsersNotFoundException extends ServiceException {

    public UsersNotFoundException() {
        super();
    }

    public UsersNotFoundException(String string) {
        super(string);
    }

    public UsersNotFoundException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public UsersNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
