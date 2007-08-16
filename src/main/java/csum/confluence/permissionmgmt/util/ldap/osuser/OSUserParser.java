/**
 * Copyright (c) 2007, Custom Space Usergroups Manager Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Custom Space Usergroups Manager Development Team
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package csum.confluence.permissionmgmt.util.ldap.osuser;

import csum.confluence.permissionmgmt.util.ldap.ILdapEnvironmentProvider;
import csum.confluence.permissionmgmt.util.ldap.LDAPException;
import csum.confluence.permissionmgmt.util.ldap.LDAPLookup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class to handle manipulation of the osuser.xml file, to allow the configuration for LDAP connections to be extractable
 * given the class name used.  Eg an osuser.xml file of:
 * <p/>
 * <pre>
 *   &lt;provider class="com.opensymphony.user.provider.ldap.LDAPCredentialsProvider"&gt;
 * 	    &lt;property name="java.naming.factory.initial"&gt;com.sun.jndi.ldap.LdapCtxFactory&lt;/property&gt;
 * 	    &lt;property name="java.naming.provider.url"&gt;ldap://myldap.server.net:389&lt;/property&gt;
 * 	    &lt;property name="searchBase"&gt;DC=this,DC=that&lt;/property&gt;
 * 	    &lt;property name="uidSearchName"&gt;sAMAccountName&lt;/property&gt;
 * <p/>
 * 	    &lt;property name="java.naming.security.principal"&gt;CN=this,DC=that&lt;/property&gt;
 * 	    &lt;property name="java.naming.security.credentials"&gt;somepass&lt;/property&gt;
 * 	    &lt;property name="java.naming.referral"&gt;follow&lt;/property&gt;
 * 	    &lt;property name="exclusive-access"&gt;true&lt;/property&gt;
 *   &lt;/provider&gt;
 * </pre>
 * <p/>
 * Would be parsed out by using <code>getProviderProperties</code> or <code>getProviderPropertiesAsHash</code> to
 * extract the property key/value elements.
 *
 * @author Andy Brook
 */
public class OSUserParser extends DefaultHandler implements ILdapEnvironmentProvider {
    private static final Log LOG = LogFactory.getLog(OSUserParser.class);
    
	public static final String OSUSER_CREDENTIAL_PROVIDER="com.opensymphony.user.provider.ldap.LDAPCredentialsProvider";
	public static final String OSUSER_XML_FILENAME="osuser.xml";
    public static final String NAME = "name";
    
    private static OSUserParser gInstance;

    public static class OSUserElement {
        String name;

        //use hashtable to can use to init LDAP environment directly
        Hashtable attributes = new Hashtable();
        String cdata;
        List properties = new ArrayList();
    }
	
    private List fProviders =new ArrayList();
    private OSUserElement fCurrentProviderElement = null;
    private OSUserElement fCurrentPropertyElement = null;
    
    /** containing the fully parsed content of the osuser.xml file */
    private Hashtable fEnvironment=null;

    /** containing the fully qualified provider class to use*/
    private String fRequiredClass;    
	
    protected OSUserParser(){}
    
	public static OSUserParser getInstance()
	{
		if (gInstance==null)
		{
			gInstance=new OSUserParser();
		}
		return gInstance;
	}
    
    public List getProviderProperties(String requiredClass) {
        OSUserElement el = getProvider(requiredClass);
        List ret = null;

        if (el == null) {
            LOG.warn("no provider found: " + requiredClass + ", of " + (fProviders != null ? (fProviders.size() + " : " + fProviders.toString()) : "0"));
        } else {
            ret = el.properties;
        }
        return ret;
    }

    /**
     * method to return the properties of the associated class type to be parsed into a hashtable,
     * suitable for use in creating an LDAP connection
     *
     * @param requiredClass
     * @return hashtable, mapping environment for LDAP connections.  Or null, if no properties found or class not found.
     */
    public Hashtable getProviderPropertiesAsHash(String requiredClass) {
        Hashtable env = null;
        List propertiesList = getProviderProperties(requiredClass);

        if (propertiesList == null || propertiesList.size() == 0) {
            LOG.error("Unable to load LDAP attributes from osuser.xml");
        } else {
            env = new Hashtable();
            for (int i = 0; i < propertiesList.size(); i++) {
                OSUserElement userElement = (OSUserElement) propertiesList.get(i);
                if (userElement.attributes.containsKey(NAME)) {
                    String key = (String) userElement.attributes.get(NAME);

                    LOG.debug("Adding ldap property: " + key + " = " + userElement.cdata);
                    env.put(key, userElement.cdata);
                                        
                } else {
                    LOG.error("can't find key for " + userElement);
                }
            }
            
            //commonalise searchBase/baseSearch as 'baseContext'                    
            env.put(LDAPLookup.SEARCH_BASE, env.get("searchBase"));
        }
        return env;
    }


    /**
     * return parsed element or null if not found
     *
     * @param requiredClass
     * @return osUserElement containing the hierarchy of properties found for the given class
     */
    public OSUserElement getProvider(String requiredClass) {
        for (Iterator iterator = fProviders.iterator(); iterator.hasNext();) {
            OSUserElement el = (OSUserElement) iterator.next();
            for (Iterator attributeIter = el.attributes.keySet().iterator(); attributeIter.hasNext();) {
                String attName = (String) attributeIter.next();
                if (attName.equalsIgnoreCase("class")) {
                    if (((String) el.attributes.get(attName)).equalsIgnoreCase(requiredClass)) {
                        return el;
                    }
                }
            }
        }
        return null;
    }
    
    /** Method to retrieve all the provider classes listed in the osuser.xml file
     * 
     * @return List of fully qualified classes
     */
    public List getProviderClasses()
    {
    	ArrayList providerClasses=new ArrayList();
        for (Iterator iterator = fProviders.iterator(); iterator.hasNext();) {
            OSUserElement el = (OSUserElement) iterator.next();
            for (Iterator attributeIter = el.attributes.keySet().iterator(); attributeIter.hasNext();) {
                String attName = (String) attributeIter.next();
                if (attName.equalsIgnoreCase("class")) {
                	Object o=el.attributes.get(attName);
                    String provider=(String) o;
                    providerClasses.add(provider);
                }
            }
        }
        
        return providerClasses;
    }
    
    /** Method to set the provider class declared in the osuser.xml file, it should be a subset of the available providers
     * 
     * @param providerClass fully qualified provider class
     */
    public void setRequiredProvider(String providerClass)
    {
    	fRequiredClass=providerClass;
    }

    /** kick off the parser, ensuring the classloader for resource loading
     * is retrieved from the current thread
     * 
     * @throws ParserConfigurationException
     */
    public void parse() throws ParserConfigurationException
    {    	
		//according to http://confluence.atlassian.com/pages/viewpage.action?pageId=200934
		InputStream is = com.atlassian.core.util.ClassLoaderUtils.getResourceAsStream(OSUSER_XML_FILENAME, getClass());

		if (is!=null)
		{
        	parseImpl( is );
        	LOG.info(OSUSER_XML_FILENAME+" Parse completed.");
		}
		else
		{
			String msg="The "+OSUSER_XML_FILENAME+" file could not be found.";
			LOG.error(msg);
			throw new ParserConfigurationException(msg);
		}
    }
    
    /** internal method to parse the resource
     * 
     * @param is
     * @throws ParserConfigurationException
     */
    private void parseImpl(InputStream is) throws ParserConfigurationException {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(is, this);
        }
        catch (ParserConfigurationException pce) {
            LOG.error(pce);
            throw new ParserConfigurationException("OSUserParser failed with a general configuration parse problem: " + pce.getLocalizedMessage());
        }
        catch (SAXException saxe) {
            LOG.error(saxe);
            throw new ParserConfigurationException("OSUserParser failed with a SAX parse problem: " + saxe.getLocalizedMessage());
        }
        catch (IOException e) {
            LOG.error(e);
            throw new ParserConfigurationException("OSUserParser failed with an IO problem: " + e.getLocalizedMessage());
        }
    }

    /** SAX Parse method
     * 
     */
    public void characters(char[] chars, int offset, int len) throws SAXException {
        String s = new String(chars, offset, len);
        LOG.debug("characters: " + s);
        if (fCurrentPropertyElement != null) {
            LOG.debug("Got characters: " + s);
            if (fCurrentPropertyElement.cdata == null) {
                fCurrentPropertyElement.cdata = s;
            } else {
                fCurrentPropertyElement.cdata = fCurrentPropertyElement.cdata + s;
            }
        }
    }

    /** SAX Parse method
     * 
     */
    public void endElement(String uri, String name, String elName) throws SAXException {
        LOG.debug("END el: uri=" + uri + ", name=" + name + ", elName=" + elName);
        if (elName.equals("authenticator")) {
            // ignore
        }
        if (elName.equals("provider")) {
            // handle
            LOG.debug("Handle provider el");
            fProviders.add(fCurrentProviderElement);
            fCurrentProviderElement = null;
        } else if (fCurrentPropertyElement != null && elName.equals("property")) {
            fCurrentProviderElement.properties.add(fCurrentPropertyElement);
            fCurrentPropertyElement = null;
        }
    }

    /** SAX Parse method
     * 
     */
    public void startElement(String uri, String name, String elName, Attributes atts) throws SAXException {
        LOG.debug("START el: uri=" + uri + ", name=" + name + ", elName=" + elName + ", atts=" + atts.toString());
        if (elName.equals("authenticator")) {
            // ignore
        }
        if (elName.equals("provider")) {
            // handle
            LOG.debug("handling provider el");
            fCurrentProviderElement = new OSUserElement();
            fCurrentProviderElement.name = elName;
            addAtts(atts, fCurrentProviderElement.attributes);
        } else if (elName.equals("property")) {
            LOG.debug("handling property el");
            fCurrentPropertyElement = new OSUserElement();
            fCurrentPropertyElement.name = "property";
            addAtts(atts, fCurrentPropertyElement.attributes);
        }
    }

    /** helper method
     * 
     */
    private void addAtts(Attributes atts, Map attMap) {
        int len = atts.getLength();
        LOG.debug("Atts length: " + atts.getLength() + " - " + atts.toString());
        for (int i = 0; i < len; i++) {
            String uri = atts.getURI(i);
            String qname = atts.getQName(i);
            String localName = atts.getLocalName(i);
            String type = atts.getType(i);
            String value = atts.getValue(i);

            LOG.debug("Added LDAP attribute : URI = " + uri.toString() + ", qname=" + qname + ", localName=" + localName + ", type=" + type + ", value=" + value);
            attMap.put(qname, value);
        }
    }

//    /** debug usage
//     * 
//     */
//    private void display() {
//        for (Iterator iterator = fProviders.iterator(); iterator.hasNext();) {
//            OSUserElement el = (OSUserElement) iterator.next();
//            LOG.debug("Provider: " + el.name);
//
//            //attributes of the element
//            for (Iterator iterator2 = el.attributes.keySet().iterator(); iterator2.hasNext();) {
//                String key = (String) iterator2.next();
//                String value = (String) el.attributes.get(key);
//                LOG.debug(" [att] -  " + key + " = " + value);
//            }
//
//            //sub nodes
//            for (Iterator iterator3 = el.properties.iterator(); iterator3.hasNext();) {
//                OSUserElement property = (OSUserElement) iterator3.next();
//
//                //attributes of the element
//                for (Iterator iterator4 = property.attributes.keySet().iterator(); iterator4.hasNext();) {
//                    String key = (String) iterator4.next();
//                    String value = (String) el.attributes.get(key);
//                    LOG.debug(" [att] -  " + key + " = " + value);
//                    LOG.debug("\n");
//                }
//
//                LOG.debug("cdata=" + property.cdata);
//                LOG.debug("\n");
//			}
//
//		}
//	}

    /** Implement the interface to return the environment
     * @return Hashtable
     * @throws LDAPException if it breaks
     */
    public Hashtable getLdapEnvironment() throws LDAPException
	{
		if (fEnvironment==null)
		{
			if (fRequiredClass!=null)
			{
			   	fEnvironment = getProviderPropertiesAsHash(fRequiredClass);
			}
			else
			{
				String msg="No osuser provider class has been set, see getProviders() for list";
				LOG.error(msg);
				throw new LDAPException(msg);
			}
		}
      
      return fEnvironment;
	}
}
