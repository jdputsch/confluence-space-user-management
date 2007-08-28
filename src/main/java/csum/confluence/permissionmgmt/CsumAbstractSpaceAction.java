package csum.confluence.permissionmgmt;

import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 28, 2007
 * Time: 10:30:41 AM
 */
public class CsumAbstractSpaceAction extends AbstractSpaceAction {

    public String getPluginKey()
    {
        return CustomPermissionConstants.PLUGIN_KEY;
    }
}
