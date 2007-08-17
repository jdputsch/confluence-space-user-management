package csum.confluence.permissionmgmt.util.jira;

import csum.confluence.permissionmgmt.util.jira.JiraServiceAuthenticationContext;
import csum.confluence.permissionmgmt.service.exception.ServiceAuthenticationException;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapServiceServiceLocator;
import csum.confluence.permissionmgmt.soap.jira.JiraSoapService;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigurable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.rpc.ServiceException;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 17, 2007
 * Time: 9:28:32 AM
 */
public class JiraSoapUtil {

    private static Log log = LogFactory.getLog(JiraSoapUtil.class);

    public static JiraServiceAuthenticationContext login(ServiceContext serviceContext) throws ServiceAuthenticationException {
        JiraServiceAuthenticationContext jiraServiceAuthenticationContext = new JiraServiceAuthenticationContext();
        try {
            JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
            CustomPermissionConfigurable config = serviceContext.getCustomPermissionConfigurable();
            jiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(config.getJiraSoapUrl());
            JiraSoapService jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
            jiraServiceAuthenticationContext.setJiraSoapService( jiraSoapService );
            jiraServiceAuthenticationContext.setToken( jiraSoapService.login(config.getJiraSoapUsername(), config.getJiraSoapPassword()) );
        }
        catch (Throwable t) {
            log.error("Failed Jira SOAP Authentication!", t);
            throw new ServiceAuthenticationException("Failed Jira SOAP Authentication!", t);
        }

        return jiraServiceAuthenticationContext;
    }

    public static void logout(JiraServiceAuthenticationContext jiraServiceAuthenticationContext) throws ServiceAuthenticationException {
        try {
            jiraServiceAuthenticationContext.getJiraSoapService().logout(jiraServiceAuthenticationContext.getToken());
        }
        catch (Throwable t) {
            log.error("Failed Jira SOAP Authentication!", t);
            throw new ServiceAuthenticationException("Failed Jira SOAP Authentication!", t);
        }
    }
}
