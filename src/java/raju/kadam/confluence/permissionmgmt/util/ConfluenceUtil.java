package raju.kadam.confluence.permissionmgmt.util;

import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.spring.container.ContainerManager;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 20, 2007
 * Time: 12:21:44 PM
 */
public class ConfluenceUtil {

    //TODO: getDomainName() is deprecated- find another method   
    public static String getConfluenceUrl(BootstrapManager bootStrapManager){
        //Get base URL for confluence installation
        bootStrapManager = (BootstrapManager) ContainerManager.getInstance().getContainerContext().getComponent("bootstrapManager");
        return bootStrapManager.getDomainName();
    }
}
