package raju.kadam.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * This class is used in our velocity templates.
 *
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 19, 2007
 * Time: 3:17:18 PM
 */
public class VelocityTools {

    private Log log = LogFactory.getLog(this.getClass());

    // TODO: this could be done with actual velocity tools
    public String url(String s) {
        String result = null;
        try {
            result = URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            log.error("Error URL encoding '" + s + "'", e);
        }

        return result;
    }
}
