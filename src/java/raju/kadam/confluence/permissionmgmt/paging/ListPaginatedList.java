package raju.kadam.confluence.permissionmgmt.paging;

import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;

import java.util.List;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 9, 2007
 * Time: 10:37:42 AM
 */
public class ListPaginatedList extends BasePaginatedList {

    private List list;

    public List getList() {
        return this.list;
    }

    /** starts with 1 */
    public int getPageNumber() {
        return 1;
    }

    /** total results (records, not pages) */
    public int getFullListSize() {
        return this.list.size();
    }

    public void setList(List list) {
        this.list = list;
    }
}
