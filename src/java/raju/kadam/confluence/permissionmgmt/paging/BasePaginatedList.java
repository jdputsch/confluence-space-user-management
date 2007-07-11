package raju.kadam.confluence.permissionmgmt.paging;

import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 10, 2007
 * Time: 10:04:08 AM
 */
public abstract class BasePaginatedList implements PaginatedList {

    private int objectsPerPage = PagingConstants.DEFAULT_OBJECTS_PER_PAGE;
    private String searchId;
    private String sortCriterion;
    private SortOrderEnum sortDirection = SortOrderEnum.ASCENDING;

    public int getObjectsPerPage() {
        return objectsPerPage;
    }

    public void setObjectsPerPage(int objectsPerPage) {
        if (objectsPerPage < 1) {
            throw new IllegalArgumentException("Must have at least 1 object per page.");
        }

        this.objectsPerPage = objectsPerPage;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public String getSortCriterion() {
        return sortCriterion;
    }

    public void setSortCriterion(String sortCriterion) {
        this.sortCriterion = sortCriterion;
    }

    public SortOrderEnum getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortOrderEnum sortDirection) {
        this.sortDirection = sortDirection;
    }
}
