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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.Session;

import org.slf4j.Logger;

import edu.kit.scc.regapp.mail.api.QueuedMailService;
import edu.kit.scc.regapp.mail.api.SimpleQueuedMail;
import edu.kit.scc.webreg.exc.MailServiceException;

@Stateless
public class QueuedMailServiceImpl implements QueuedMailService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger; 
	
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName = "java:/queue/bwIdmMailQueue")
	private Queue queue;

	private Connection connection;
	
	@PostConstruct
	public void connect() {
		try {
			logger.debug("Opening JMS connection");
			connection = connectionFactory.createConnection();
		} catch (JMSException e) {
			logger.error("Could not connect JMS", e);
		}
	}

	@Override
	public void sendMail(String from, String to, String cc, String bcc, String subject, String body, String replyTo) 
			throws MailServiceException {

		logger.debug("Submitting mail message to Queue");

		SimpleQueuedMail mail = new SimpleQueuedMail();
		mail.setFrom(from);
		mail.setTo(to);
		mail.setCc(cc);
		mail.setBcc(bcc);
		mail.setSubject(subject);
		mail.setBody(body);
		mail.setReplyTo(replyTo);
		
		try {
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer messageProducer = session.createProducer(queue);

			ObjectMessage message = session.createObjectMessage();
			message.setObject(mail);
			messageProducer.send(message);
			
			logger.debug("SimpleQueuedMail message submitted");

		} catch (JMSException e) {
			logger.warn("Could not send JMS Message", e);
			throw new MailServiceException("Could not send JMS Message", e);
		}
		
	}

	@PreDestroy
	public void disconnect() {
		try {
			logger.debug("Closing JMS connection");
			connection.close();
		} catch (JMSException e) {
			logger.warn("Could not connect JMS", e);
		}
	}

}
