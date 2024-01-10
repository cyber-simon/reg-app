/*******************************************************************************
 * Copyright (c) 2014 Michael Simon. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html Contributors: Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.service.mail.impl;

import java.security.KeyStore.PrivateKeyEntry;

import static edu.kit.scc.webreg.service.impl.KeyStoreService.KEYSTORE_CONTEXT_EMAIL;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;

import edu.kit.scc.regapp.mail.api.MailService;
import edu.kit.scc.webreg.exc.MailServiceException;
import edu.kit.scc.webreg.service.impl.KeyStoreService;

@Stateless
public class MailServiceImpl implements MailService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private KeyStoreService keyStoreService;

	@Resource(lookup = "java:/mail/bwIdmMail")
	private Session session;

	@Override
	public void sendMail(String from, String to, String cc, String bcc, String subject, String body, String replyTo, String signatureAlias)
			throws MailServiceException {
		logger.debug("Sending mail from {} to {}", from, to);
		try {
			MimeMessage message = createMessage(from, to, cc, bcc, subject, body, replyTo);
			if (keyStoreService.hasPrivateKeyEntry(KEYSTORE_CONTEXT_EMAIL, signatureAlias)) {
				PrivateKeyEntry privateKeyEntry = keyStoreService.fetchPrivateKeyEntry(KEYSTORE_CONTEXT_EMAIL, signatureAlias);
				message = signMessage(message, privateKeyEntry.getPrivateKey(), (X509Certificate[]) privateKeyEntry.getCertificateChain());
			}
			Transport.send(message);
		} catch (MessagingException e) {
			throw new MailServiceException(e);
		}
	}

	private MimeMessage createMessage(String from, String to, String cc, String bcc, String subject, String body, String replyTo)
			throws MailServiceException, MessagingException {
		if (from == null || to == null) {
			throw new MailServiceException("From and To may not be null. Please set From and To in Email Template");
		}
		MimeMessage message = new MimeMessage(session);
		message.setFrom(parseEmailAddressList(from)[0]);
		message.setRecipients(Message.RecipientType.TO, parseEmailAddressList(to));
		if (cc != null) {
			message.setRecipients(Message.RecipientType.CC, parseEmailAddressList(cc));
		}
		if (bcc != null) {
			message.setRecipients(Message.RecipientType.BCC, parseEmailAddressList(bcc));
		}
		if (subject != null) {
			message.setSubject(subject);
		}
		if (body != null) {
			message.setText(body);
		}
		if (replyTo != null) {
			message.setReplyTo(parseEmailAddressList(replyTo));
		}
		message.setHeader("X-Mailer", "reg-app mail service");
		message.setSentDate(new Date());
		message.saveChanges();
		return message;
	}

	private InternetAddress[] parseEmailAddressList(String emailAddressList) throws MailServiceException {
		try {
			return InternetAddress.parse(emailAddressList, false);
		} catch (AddressException e) {
			throw new MailServiceException("Invalid Email Address", e);
		}
	}

	private MimeMessage signMessage(MimeMessage message, PrivateKey privateKey, X509Certificate[] certificateChain)
			throws MessagingException, MailServiceException {
		MimeMessage signedMessage = new MimeMessage(session);
		Enumeration<String> headers = message.getAllHeaderLines();
		while (headers.hasMoreElements()) {
			signedMessage.addHeaderLine(headers.nextElement());
		}
		signedMessage.setContent(signMessage(message, getSmimeSignatureGenerator(privateKey, certificateChain)));
		signedMessage.saveChanges();

		return signedMessage;
	}

	private MimeMultipart signMessage(MimeMessage message, SMIMESignedGenerator signatureGenerator) throws MailServiceException {
		try {
			return signatureGenerator.generate(message);
		} catch (SMIMEException e) {
			throw new MailServiceException("Could not sign message", e);
		}
	}

	private SMIMESignedGenerator getSmimeSignatureGenerator(PrivateKey privateKey, X509Certificate[] certificateChain)
			throws MailServiceException {
		SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
		capabilities.addCapability(SMIMECapability.aES128_CBC);
		capabilities.addCapability(SMIMECapability.aES192_CBC);
		capabilities.addCapability(SMIMECapability.aES256_CBC);

		ASN1EncodableVector attributes = new ASN1EncodableVector();
		attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(new IssuerAndSerialNumber(
				new X500Name(certificateChain[0].getIssuerDN().getName()), certificateChain[0].getSerialNumber())));
		attributes.add(new SMIMECapabilitiesAttribute(capabilities));

		SMIMESignedGenerator signer = new SMIMESignedGenerator();
		try {
			signer.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC")
					.setSignedAttributeGenerator(new AttributeTable(attributes))
					.build("DSA".equals(privateKey.getAlgorithm()) ? "SHA256withDSA" : "SHA256withRSA", privateKey, certificateChain[0]));

			signer.addCertificates(new JcaCertStore(List.of(certificateChain[0])));
		} catch (CertificateEncodingException | OperatorCreationException e) {
			throw new MailServiceException("Could not create signature generator", e);
		}

		return signer;
	}

}
