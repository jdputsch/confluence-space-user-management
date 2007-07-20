package raju.kadam.confluence.permissionmgmt.config;

import java.util.Map;
import java.util.TreeMap;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 20, 2007
 * Time: 8:16:33 AM
 */
public class ConfigValidationResponse {

    private boolean isValid;

    // using TreeMap to sort by fieldName
    private Map fieldNameToErrorMessage = new TreeMap();

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public Map getFieldNameToErrorMessage() {
        return fieldNameToErrorMessage;
    }

    public void setFieldNameToErrorMessage(Map fieldNameToErrorMessage) {
        this.fieldNameToErrorMessage = fieldNameToErrorMessage;
    }

    public void addFieldError(String fieldName, String errorMessage) {
        fieldNameToErrorMessage.put(fieldName, errorMessage);
    }
}
