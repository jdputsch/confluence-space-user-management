/**
 * Copyright (c) 2007, Custom Space Usergroups Manager Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Custom Space Usergroups Manager Development Team
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package csum.confluence.permissionmgmt.util.paging;

import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Gary S. Weaver
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
