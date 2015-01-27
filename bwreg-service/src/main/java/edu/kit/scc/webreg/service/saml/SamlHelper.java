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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.namespace.QName;

import org.opensaml.Configuration;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSDateTime;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Named("samlHelper")
@ApplicationScoped
public class SamlHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	protected SecureRandomIdentifierGenerator randomIdGen;
	
	protected MarshallerFactory marshallerFactory;
	protected UnmarshallerFactory unmarshallerFactory;
	protected BasicParserPool basicParserPool;
	
	@PostConstruct
	public void init() {
		try {
			randomIdGen = new SecureRandomIdentifierGenerator();
		} catch (NoSuchAlgorithmException e) {
			logger.error("No SecureRandomIdentifierGenerator available", e);
		}
		
		marshallerFactory = Configuration.getMarshallerFactory();
		unmarshallerFactory = Configuration.getUnmarshallerFactory();
		basicParserPool = new BasicParserPool();
		basicParserPool.setNamespaceAware(true);
	}
	
	public String getRandomId() {
		return randomIdGen.generateIdentifier();
	}

	@SuppressWarnings ("unchecked")
	public <T> T create (Class<T> cls, QName qname)
	{
	  return (T) ((XMLObjectBuilder<?>) Configuration.getBuilderFactory()
			  .getBuilder (qname)).buildObject (qname);
	}
	
	public <T extends XMLObject> String marshal(T t) {
		try {
			Element element = toXmlElement(t);
			return XMLHelper.nodeToString(element);
		} catch (MarshallingException e) {
			logger.error("No Marshalling possible", e);
			return null;
		}
	}

	public <T extends XMLObject> String prettyPrint(T t) {
		try {
			Element element = toXmlElement(t);
			return XMLHelper.prettyPrintXML(element);
		} catch (MarshallingException e) {
			logger.error("No Marshalling possible", e);
			return null;
		}
	}
	
	public <T extends XMLObject> Element toXmlElement(T t) throws MarshallingException {
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
			}
		}
		return returnList;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends XMLObject> T unmarshal(String s, Class<T> c) {
		try {
			Document document = basicParserPool.parse(new StringReader(s));
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
			XMLObject xmlObject = unmarshaller.unmarshall(document.getDocumentElement());
			return (T) xmlObject;
		} catch (XMLParserException e) {
			logger.error("No Unmarshalling possible", e);
			return null;
		} catch (UnmarshallingException e) {
			logger.error("No Unmarshalling possible", e);
			return null;
		}
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
	
	public SecureRandomIdentifierGenerator getRandomIdGen() {
		return randomIdGen;
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
}
