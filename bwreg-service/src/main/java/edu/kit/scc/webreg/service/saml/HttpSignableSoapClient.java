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

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.Charset;

import net.shibboleth.utilities.java.support.xml.ParserPool;

import org.apache.http.client.HttpClient;
import org.opensaml.core.config.Configuration;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.soap.client.SOAPClientException;
import org.opensaml.soap.client.http.HttpSOAPClient;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class HttpSignableSoapClient extends HttpSOAPClient implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(HttpSignableSoapClient.class);
	
	private Signature signature;
	
    public HttpSignableSoapClient(HttpClient client, ParserPool parser, Signature signature) {
		super(client, parser);
		this.signature = signature;
	}

    @Override
	protected RequestEntity createRequestEntity(Envelope message, Charset charset) throws SOAPClientException {
        try {
            Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(message);
            ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(arrayOut, charset);

            Element element = marshaller.marshall(message);
            try {
				Signer.signObject(signature);
			} catch (SignatureException e) {
				throw new SOAPClientException(e);
			}
            
            if (logger.isDebugEnabled()) {
                logger.debug("Outbound SOAP message is:\n" + XMLHelper.prettyPrintXML(element));
            }
            XMLHelper.writeNode(element, writer);
            return new ByteArrayRequestEntity(arrayOut.toByteArray(), "text/xml");
        } catch (MarshallingException e) {
            throw new SOAPClientException("Unable to marshall SOAP envelope", e);
        }
    }	
}
