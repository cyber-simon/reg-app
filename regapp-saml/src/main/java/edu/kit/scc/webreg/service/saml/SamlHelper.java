/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.service.saml;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.RandomStringUtils;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSDateTime;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.xml.SerializeSupport;
import net.shibboleth.shared.xml.XMLParserException;
import net.shibboleth.shared.xml.impl.BasicParserPool;

@Named("samlHelper")
@ApplicationScoped
public class SamlHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	protected MarshallerFactory marshallerFactory;
	protected UnmarshallerFactory unmarshallerFactory;
	protected BasicParserPool basicParserPool;
	protected XMLObjectBuilderFactory builderFactory;
	
	@PostConstruct
	public void init() {
		basicParserPool = new BasicParserPool();
		basicParserPool.setNamespaceAware(true);
		try {
			basicParserPool.initialize();
		} catch (ComponentInitializationException e) {
			logger.error("Init of ParserPool failed", e);
		}
		
        XMLObjectProviderRegistry registry;
        synchronized(ConfigurationService.class) {
            registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
            if (registry == null) {
                registry = new XMLObjectProviderRegistry();
                ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
            }
        }
        registry.setParserPool(basicParserPool);

		marshallerFactory = registry.getMarshallerFactory();
		unmarshallerFactory = registry.getUnmarshallerFactory();
		builderFactory = registry.getBuilderFactory();
	}
	
	public String getRandomId() {
		return RandomStringUtils.randomAlphabetic(1).toLowerCase() +  UUID.randomUUID().toString();
	}

	@SuppressWarnings ("unchecked")
	public <T> T create (Class<T> cls, QName qname)
	{
	  return (T) ((XMLObjectBuilder<?>)  builderFactory.getBuilder(qname)).buildObject(qname);
	}
	
	@SuppressWarnings ("unchecked")
	public <T> T create (Class<T> cls, QName typeName, QName qname)
	{
	  return (T) ((XMLObjectBuilder<?>) builderFactory.getBuilder(typeName)).buildObject(qname, typeName);
	}
	
	public String marshal(XMLObject t) {
		try {
			Element element = toXmlElement(t);
			return SerializeSupport.nodeToString(element);
		} catch (MarshallingException e) {
			logger.error("No Marshalling possible", e);
			return null;
		}
	}

	public String prettyPrint(XMLObject t) {
		try {
			Element element = toXmlElement(t);
			return SerializeSupport.prettyPrintXML(element);
		} catch (MarshallingException e) {
			logger.error("No Marshalling possible", e);
			return null;
		}
	}
	
	public Element toXmlElement(XMLObject t) throws MarshallingException {
		Marshaller marshaller = marshallerFactory.getMarshaller(t);
		return marshaller.marshall(t);
	}	
	
	public List<Object> getAttribute(Attribute attribute) {
		List<XMLObject> avList = attribute.getAttributeValues();

		List<Object> returnList = new ArrayList<Object>(avList.size());
		for (XMLObject obj : avList) {
			if (obj != null) {
				if (obj instanceof XSString) {
					returnList.add(((XSString) obj).getValue());
				}
				else if (obj instanceof XSDateTime) {
					returnList.add(((XSDateTime) obj).getValue());
				}
				/*
				 * Support Attributes with no encoded type. They come as XSAny.
				 * Assume it's a string
				 */
				else if (obj instanceof XSAny) {
					XSAny any = (XSAny) obj;
					if (any.getTextContent() != null) {
						returnList.add(any.getTextContent().trim());
					}
				}
				else {
					logger.info("Unknown Attribute type: {}", obj.getClass());
				}
			}
		}
		return returnList;
	}
	
	public <T extends XMLObject> T unmarshal(String s, Class<T> c) {
		return unmarshal(s, c, null);
	}
	
	public <T extends XMLObject> T unmarshalThrow(String s, Class<T> c) 
			throws XMLParserException, UnmarshallingException {
		return unmarshalThrow(s, c, null);
	}
	
	public <T extends XMLObject> T unmarshal(String s, Class<T> c, Auditor auditor) {
		try {
			return (T) unmarshalThrow(s, c, auditor);
		} catch (XMLParserException e) {
			logger.error("No Unmarshalling possible", e);
			if (auditor != null) {
				auditor.logAction(c.getClass().getName(), "XML UNMARSHALL", s, e.getMessage(), AuditStatus.FAIL);
			}
			return null;
		} catch (UnmarshallingException e) {
			logger.error("No Unmarshalling possible", e);
			if (auditor != null) {
				auditor.logAction(c.getClass().getName(), "XML UNMARSHALL", s, e.getMessage(), AuditStatus.FAIL);
			}
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends XMLObject> T unmarshalThrow(String s, Class<T> c, Auditor auditor) 
			throws XMLParserException, UnmarshallingException {
		Document document = basicParserPool.parse(new StringReader(s));
		Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
		XMLObject xmlObject = unmarshaller.unmarshall(document.getDocumentElement());
		return (T) xmlObject;
	}

	public Map<String, Attribute> assertionToAttributeMap(Assertion assertion) {
		Map<String, Attribute> attrMap = new HashMap<String, Attribute>();
		
		for (AttributeStatement attrStatement : assertion.getAttributeStatements()) {
			for (Attribute attr : attrStatement.getAttributes()) {
				String attrName = attr.getName();
				attrMap.put(attrName, attr);
			}
		}
	
		return attrMap;
	}
	
	public MarshallerFactory getMarshallerFactory() {
		return marshallerFactory;
	}

	public UnmarshallerFactory getUnmarshallerFactory() {
		return unmarshallerFactory;
	}

	public BasicParserPool getBasicParserPool() {
		return basicParserPool;
	}

	public XMLObjectBuilderFactory getBuilderFactory() {
		return builderFactory;
	}
}
