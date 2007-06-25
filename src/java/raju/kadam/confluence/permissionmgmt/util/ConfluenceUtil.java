package raju.kadam.confluence.permissionmgmt.util;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.spring.container.ContainerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 20, 2007
 * Time: 12:21:44 PM
 */
public class ConfluenceUtil {

    private static Log log = LogFactory.getLog(ConfluenceUtil.class);

    public static String getConfluenceUrl(SettingsManager settingsManager) {
        
        //Get base URL for confluence installation
        return settingsManager.getGlobalSettings().getBaseUrl();
    }

    public static Object loadComponentWithRetry(String component) {
        Object result = null;
        int triesLeft = 5;
        while (result == null && triesLeft > 0) {
            log.debug("attempting to load '" + component + "'. tries left = " + triesLeft);
            result = loadComponentOrReturnNullCatchingThrowable(component);
            triesLeft--;

            if (result==null) {
                try {
                    // wait 0-1000 msec
                    log.debug("waiting before next try");
                    Thread.sleep((int)(Math.random() * 1000D));
                }
                catch (InterruptedException ie) {
                    //don't care
                }
            }
        }

        if (result==null) {
            //one last try

            // maybe this one will work, or maybe it will throw an error
            result = ContainerManager.getComponent(component);
            log.debug("successfully loaded " + component);
        }

        return result;
    }

    private static Object loadComponentOrReturnNullCatchingThrowable(String component) {
        Object result = null;
        try {
            result = ContainerManager.getComponent(component);
            log.debug("successfully loaded " + component);
        }
        catch (Throwable t) {
            //don't care
            log.debug("failed to load " + component);
        }
        return result;
    }
}