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

import net.shibboleth.utilities.java.support.xml.ParserPool;

import org.apache.http.client.HttpClient;
import org.opensaml.soap.client.http.HttpSOAPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSignableSoapClient extends HttpSOAPClient implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(HttpSignableSoapClient.class);
	
    public HttpSignableSoapClient(HttpClient client, ParserPool parser) {
		super();
		setHttpClient(client);
		setParserPool(parser);
	}
 
//    @Override
//	protected RequestEntity createRequestEntity(Envelope message, Charset charset) throws SOAPClientException {
//        try {
//            Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(message);
//            ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
//
//            Element element = marshaller.marshall(message);
//            try {
//				Signer.signObject(signature);
//			} catch (SignatureException e) {
//				throw new SOAPClientException(e);
//			}
//            
//            if (logger.isDebugEnabled()) {
//                logger.debug("Outbound SOAP message is:\n" + SerializeSupport.prettyPrintXML(element));
//            }
//            SerializeSupport.writeNode(element, arrayOut);
//            return new ByteArrayRequestEntity(arrayOut.toByteArray(), "text/xml");
//        } catch (MarshallingException e) {
//            throw new SOAPClientException("Unable to marshall SOAP envelope", e);
//        }
//    }	
}
