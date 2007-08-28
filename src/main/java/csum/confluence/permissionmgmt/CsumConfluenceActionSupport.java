package csum.confluence.permissionmgmt;

import com.atlassian.confluence.core.ConfluenceActionSupport;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 28, 2007
 * Time: 10:27:55 AM
 */
public abstract class CsumConfluenceActionSupport extends ConfluenceActionSupport {

    public String getPluginKey()
    {
        return CustomPermissionConstants.PLUGIN_KEY;
    }
}
