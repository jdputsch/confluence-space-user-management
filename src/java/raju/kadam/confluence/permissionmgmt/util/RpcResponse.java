package raju.kadam.confluence.permissionmgmt.util;

import java.util.Vector;
import java.util.List;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 20, 2007
 * Time: 3:17:50 PM
 */
public class RpcResponse {

    private boolean isError;
    private List messages;


    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public List getMessages() {
        return messages;
    }

    public void setMessages(List messages) {
        this.messages = messages;
    }
}
