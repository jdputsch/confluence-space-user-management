package raju.kadam.confluence.permissionmgmt.paging;

import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;

import java.util.List;

import com.atlassian.user.search.page.Pager;
import com.atlassian.user.search.page.PagerUtils;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 9, 2007
 * Time: 10:12:57 AM
 */
public class PagerPaginatedList extends BasePaginatedList {

    private Pager pager;

    public List getList() {
        return PagerUtils.toList(this.pager);
    }

    /** starts with 1 */
    public int getPageNumber() {
        return this.pager.getIndex() + 1;
    }

    /** total results (records, not pages) */
    public int getFullListSize() {
        return PagerUtils.count(this.pager);
    }

    public Pager getPager() {
        return pager;
    }

    public void setPager(Pager pager) {
        this.pager = pager;
    }
}
