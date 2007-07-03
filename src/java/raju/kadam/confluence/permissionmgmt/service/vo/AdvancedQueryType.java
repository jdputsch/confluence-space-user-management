package raju.kadam.confluence.permissionmgmt.service.vo;

import com.atlassian.user.search.query.TermQuery;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 21, 2007
 * Time: 11:42:42 AM
 */
public interface AdvancedQueryType {

    public static final String SUBSTRING_CONTAINS = TermQuery.SUBSTRING_CONTAINS;
    public static final String SUBSTRING_ENDS_WITH = TermQuery.SUBSTRING_ENDS_WITH;
    public static final String SUBSTRING_STARTS_WITH = TermQuery.SUBSTRING_STARTS_WITH;
}
