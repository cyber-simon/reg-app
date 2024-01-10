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
package edu.kit.scc.webreg.service.mail;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;

import org.slf4j.Logger;

import edu.kit.scc.regapp.mail.api.MailService;
import edu.kit.scc.regapp.mail.api.SimpleQueuedMail;
import edu.kit.scc.webreg.exc.MailServiceException;

/**
 * Message-Driven Bean implementation class for: MailSenderMdb
 */
@MessageDriven(
		activationConfig = { 
				@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"), 
				@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/bwIdmMailQueue")
		})
public class MailSenderMdb implements MessageListener {
	
	@Inject
	private Logger logger;
	
	@Inject
	private MailService mailService;
	
    public void onMessage(Message message) {
    	logger.debug("MailSenderMdb processing message");
    	
    	if (message instanceof ObjectMessage) {
			ObjectMessage om = (ObjectMessage) message;

			try {
				Object o = om.getObject();
     			if (o instanceof SimpleQueuedMail) {
     		    	logger.debug("message is a SimpleQueuedMail");

     		    	SimpleQueuedMail mail = (SimpleQueuedMail) o;
     				mailService.sendMail(mail.getFrom(), mail.getTo(), mail.getCc(), 
     						mail.getBcc(), mail.getSubject(), mail.getBody(), mail.getReplyTo(),
     						mail.getSignatureAlias());
     			}
     	    	else {
     	    		logger.warn("Nothing to do for class {}. Ignore.", message.getClass());
     	    	}
     			
			} catch (JMSException e) {
				logger.warn("JMS Exception happened while trying to fetch Object", e);
			} catch (MailServiceException e) {
				logger.warn("MailService could not send Mail", e);
			}
    	}
    	else {
    		logger.warn("Message not an object message. Ignore.");
    	}
    }

}
