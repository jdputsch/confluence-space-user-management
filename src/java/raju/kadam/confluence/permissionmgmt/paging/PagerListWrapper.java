package raju.kadam.confluence.permissionmgmt.paging;

import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 9, 2007
 * Time: 2:18:03 PM
 */
public class PagerListWrapper implements List {

    private Log log = LogFactory.getLog(this.getClass());

    Pager pager;
    List pagerAsList;

    public int size() {
        log.warn("size() called");
        return PagerUtils.count(pager);
    }

    public boolean isEmpty() {
        log.warn("isEmpty() called");
        return pager.isEmpty();
    }

    public boolean contains(Object o) {
        log.warn("contains() called");
        return getPagerAsList().contains(o);
    }

    public Iterator iterator() {
        log.warn("iterator() called");
        return getPagerAsList().iterator();
    }

    public Object[] toArray() {
        log.warn("toArray() called");
        return getPagerAsList().toArray();
    }

    public boolean add(Object o) {
        log.warn("add() called");
        return getPagerAsList().add(o);
    }

    public boolean remove(Object o) {
        log.warn("remove() called");
        return getPagerAsList().remove(o);
    }

    public boolean addAll(Collection collection) {
        log.warn("addAll(collection) called");
        return getPagerAsList().add(collection);
    }

    public boolean addAll(int i, Collection collection) {
        log.warn("addAll(i,collection) called");
        return getPagerAsList().addAll(i,collection);
    }

    public void clear() {
        log.warn("clear() called");
        getPagerAsList().clear();
    }

    public Object get(int i) {
        log.warn("get() called");
        return getPagerAsList().get(i);
    }

    public Object set(int i, Object o) {
        log.warn("set() called");
        return getPagerAsList().set(i,o);
    }

    public void add(int i, Object o) {
        log.warn("add() called");
        getPagerAsList().add(i,o);
    }

    public Object remove(int i) {
        log.warn("remove() called");
        return getPagerAsList().remove(i);
    }

    public int indexOf(Object o) {
        log.warn("indexOf() called");
        return getPagerAsList().indexOf(o);
    }

    public int lastIndexOf(Object o) {
        log.warn("lastIndexOf() called");
        return getPagerAsList().lastIndexOf(o);
    }

    public ListIterator listIterator() {
        log.warn("listIterator() called");
        return getPagerAsList().listIterator();
    }

    public ListIterator listIterator(int i) {
        log.warn("listIterator(i) called");
        return getPagerAsList().listIterator(i);
    }

    public List subList(int i, int i1) {
        log.warn("subList() called");
        return getPagerAsList().subList(i,i1);
    }

    public boolean retainAll(Collection collection) {
        log.warn("retainAll() called");
        return getPagerAsList().retainAll(collection);
    }

    public boolean removeAll(Collection collection) {
        log.warn("removeAll() called");
        return getPagerAsList().removeAll(collection);
    }

    public boolean containsAll(Collection collection) {
        log.warn("containsAll() called");
        return getPagerAsList().containsAll(collection);
    }

    public Object[] toArray(Object[] objects) {
        log.warn("toArray() called");
        return getPagerAsList().toArray(objects);
    }


    public Pager getPager() {
        return pager;
    }

    public void setPager(Pager pager) {
        this.pager = pager;
    }

    private List getPagerAsList() {
        if (this.pagerAsList==null) {
            this.pagerAsList = PagerUtils.toList(this.pager);
        }
        return this.pagerAsList;
    }
}
