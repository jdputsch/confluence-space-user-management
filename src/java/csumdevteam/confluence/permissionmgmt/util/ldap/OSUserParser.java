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

package csumdevteam.confluence.permissionmgmt.util.ldap;

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
import java.util.*;

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
public class OSUserParser extends DefaultHandler {
    private static final Log log = LogFactory.getLog(OSUserParser.class);
    public static final String NAME = "name";

    public static class OSUserElement {
        String name;

        //use hashtable to can use to init LDAP environment directly
        Hashtable attributes = new Hashtable();
        String cdata;
        List properties = new ArrayList();
    }

    private List fProviders = null;
    private OSUserElement fCurrentProviderElement = null;
    private OSUserElement fCurrentPropertyElement = null;

    public static final void main(String[] args) throws ParserConfigurationException {
        OSUserParser p = new OSUserParser();
        InputStream is = p.getClass().getClassLoader().getResourceAsStream("testuser.xml");
        p.parse(is);
        log.debug("Parse ok.");

        OSUserElement el = p.getProvider("com.opensymphony.user.provider.ldap.LDAPCredentialsProvider");
        log.info("found el: " + el.name);
        return;
    }

    public List getProviderProperties(String requiredClass) {
        OSUserElement el = getProvider(requiredClass);
        List ret = null;

        if (el == null) {
            log.warn("no provider found: " + requiredClass + ", of " + (fProviders != null ? (fProviders.size() + " : " + fProviders.toString()) : "0"));
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
            log.error("Unable to load LDAP attributes from osuser.xml");
        } else {
            env = new Hashtable();
            for (int i = 0; i < propertiesList.size(); i++) {
                OSUserElement userElement = (OSUserElement) propertiesList.get(i);
                if (userElement.attributes.containsKey(NAME)) {
                    String key = (String) userElement.attributes.get(NAME);

                    log.debug("Adding ldap property: " + key + " = " + userElement.cdata);
                    env.put(key, userElement.cdata);
                } else {
                    log.error("can't find key for " + userElement);
                }
            }
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

    public OSUserParser() {
        fProviders = new ArrayList();
    }

    public void parse(InputStream is) throws ParserConfigurationException {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(is, this);
        }
        catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            throw new ParserConfigurationException("OSUserParser failed with a general configuration parse problem: " + pce.getLocalizedMessage());
        }
        catch (SAXException saxe) {
            saxe.printStackTrace();
            throw new ParserConfigurationException("OSUserParser failed with a SAX parse problem: " + saxe.getLocalizedMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new ParserConfigurationException("OSUserParser failed with an IO problem: " + e.getLocalizedMessage());
        }
    }

    public void characters(char[] chars, int offset, int len) throws SAXException {
        String s = new String(chars, offset, len);
        log.info("characters: " + s);
        if (fCurrentPropertyElement != null) {
            log.info("Got characters: " + s);
            if (fCurrentPropertyElement.cdata == null) {
                fCurrentPropertyElement.cdata = s;
            } else {
                fCurrentPropertyElement.cdata = fCurrentPropertyElement.cdata + s;
            }
        }
    }

    public void endElement(String uri, String name, String elName) throws SAXException {
        log.info("END el: uri=" + uri + ", name=" + name + ", elName=" + elName);
        if (elName.equals("authenticator")) {
            // ignore
        }
        if (elName.equals("provider")) {
            // handle
            log.debug("Handle provider el");
            fProviders.add(fCurrentProviderElement);
            fCurrentProviderElement = null;
        } else if (fCurrentPropertyElement != null && elName.equals("property")) {
            fCurrentProviderElement.properties.add(fCurrentPropertyElement);
            fCurrentPropertyElement = null;
        }
    }

    public void startElement(String uri, String name, String elName, Attributes atts) throws SAXException {
        log.info("START el: uri=" + uri + ", name=" + name + ", elName=" + elName + ", atts=" + atts.toString());
        if (elName.equals("authenticator")) {
            // ignore
        }
        if (elName.equals("provider")) {
            // handle
            log.debug("handling provider el");
            fCurrentProviderElement = new OSUserElement();
            fCurrentProviderElement.name = elName;
            addAtts(atts, fCurrentProviderElement.attributes);
        } else if (elName.equals("property")) {
            log.debug("handling property el");
            fCurrentPropertyElement = new OSUserElement();
            fCurrentPropertyElement.name = "property";
            addAtts(atts, fCurrentPropertyElement.attributes);
        }
    }

    private void addAtts(Attributes atts, Map attMap) {
        int len = atts.getLength();
        log.info("Atts length: " + atts.getLength() + " - " + atts.toString());
        for (int i = 0; i < len; i++) {
            String uri = atts.getURI(i);
            String qname = atts.getQName(i);
            String localName = atts.getLocalName(i);
            String type = atts.getType(i);
            String value = atts.getValue(i);

            log.info("Added LDAP attribute : URI = " + uri.toString() + ", qname=" + qname + ", localName=" + localName + ", type=" + type + ", value=" + value);
            attMap.put(qname, value);
        }
    }

    public void display() {
        for (Iterator iterator = fProviders.iterator(); iterator.hasNext();) {
            OSUserElement el = (OSUserElement) iterator.next();
            log.debug("Provider: " + el.name);

            //attributes of the element
            for (Iterator iterator2 = el.attributes.keySet().iterator(); iterator2.hasNext();) {
                String key = (String) iterator2.next();
                String value = (String) el.attributes.get(key);
                log.debug(" [att] -  " + key + " = " + value);
            }

            //sub nodes
            for (Iterator iterator3 = el.properties.iterator(); iterator3.hasNext();) {
                OSUserElement property = (OSUserElement) iterator3.next();

                //attributes of the element
                for (Iterator iterator4 = property.attributes.keySet().iterator(); iterator4.hasNext();) {
                    String key = (String) iterator4.next();
                    String value = (String) el.attributes.get(key);
                    log.debug(" [att] -  " + key + " = " + value);
                    log.debug("\n");
                }

                log.debug("cdata=" + property.cdata);
                log.debug("\n");
			}

		}
	}
}
