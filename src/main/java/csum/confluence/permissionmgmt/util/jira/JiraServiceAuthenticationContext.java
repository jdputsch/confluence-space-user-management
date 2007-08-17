package csum.confluence.permissionmgmt.util.jira;

import csum.confluence.permissionmgmt.soap.jira.JiraSoapService;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 17, 2007
 * Time: 8:49:06 AM
 */
public class JiraServiceAuthenticationContext {

    private JiraSoapService jiraSoapService;
    private String token;

    public JiraSoapService getJiraSoapService() {
        return jiraSoapService;
    }

    public void setJiraSoapService(JiraSoapService jiraSoapService) {
        this.jiraSoapService = jiraSoapService;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
