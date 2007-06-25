package raju.kadam.confluence.permissionmgmt.service;

import raju.kadam.util.StringUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 22, 2007
 * Time: 9:36:42 AM
 */
public class ServiceException extends Exception {

    List throwables = new ArrayList();

    public ServiceException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ServiceException(String string) {
        super(string);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ServiceException(String string, Throwable throwable) {
        super(string, throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ServiceException(Throwable throwable) {
        super(throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }


    public List getThrowables() {
        return throwables;
    }

    public String getThrowablesMessagesAsCommaDelimitedString() {
        StringBuffer sb = new StringBuffer();
        if ( this.throwables != null ) {
            Iterator iter = this.throwables.iterator();
            int count = 0;
            while (iter.hasNext()) {
                Throwable throwable = (Throwable)iter.next();
                if (count!=0) {
                    sb.append(", ");
                }
                sb.append(throwable.getMessage());
                count++;
            }
        }
        return sb.toString();
    }

    public void setThrowables(List throwables) {
        this.throwables = throwables;
    }

    public void addThrowable(Throwable t) {
        throwables.add(t);
    }
}
