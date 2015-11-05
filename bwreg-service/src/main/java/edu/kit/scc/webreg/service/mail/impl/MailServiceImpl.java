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
package edu.kit.scc.webreg.service.mail.impl;

import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;

import edu.kit.scc.webreg.exc.MailServiceException;
import edu.kit.scc.webreg.service.mail.MailService;

@Stateless
public class MailServiceImpl implements MailService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Resource(lookup = "java:/mail/bwIdmMail")
	private Session session;

	@Override
	public void sendMail(String from, String to, String cc, String bcc, String subject, String body) 
			throws MailServiceException {

		logger.debug("Sending mail from {} to {}", from, to);
		
		try {
			sendMailIntern(from, to, cc, bcc, subject, body);
		} catch (AddressException e) {
			throw new MailServiceException("Invalid Email Adderss", e);
		} catch (MessagingException e) {
			throw new MailServiceException(e);
		}
	}

	private void sendMailIntern(String from, String to, String cc, String bcc, String subject, String body) 
			throws AddressException, MessagingException, MailServiceException {
		Message message = new MimeMessage(session);

		if (from == null || to == null)
			throw new MailServiceException("From or To may not be null. Please set From an To in Email Template");
		
		message.setFrom(new InternetAddress(from));

		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to, false));
		if (cc != null)
			message.setRecipients(Message.RecipientType.CC,
				InternetAddress.parse(cc, false));
		
		if (bcc != null)
			message.setRecipients(Message.RecipientType.BCC,
				InternetAddress.parse(bcc, false));

		if (subject != null)
			message.setSubject(subject);
		
		if (body != null)
			message.setText(body);
		
		message.setHeader("X-Mailer", "bwIdm Webregistrierung Mail");
		message.setSentDate(new Date());
		Transport.send(message);
	}
}
