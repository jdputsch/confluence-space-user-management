package raju.kadam.confluence.permissionmgmt.service;

import raju.kadam.util.StringUtil;

import java.util.List;
import java.util.ArrayList;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 22, 2007
 * Time: 9:56:15 AM
 */
public class IdListException extends ServiceException {

    private List ids = new ArrayList();


    public IdListException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public IdListException(String string) {
        super(string);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public IdListException(String string, Throwable throwable) {
        super(string, throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public IdListException(Throwable throwable) {
        super(throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public List getIds() {
        return ids;
    }

    public String getIdsAsCommaDelimitedString() {
        return StringUtil.convertCollectionToCommaDelimitedString(this.ids);
    }

    public void setIds(List ids) {
        this.ids = ids;
    }

    public void addId(String id) {
        ids.add(id);
    }
}
