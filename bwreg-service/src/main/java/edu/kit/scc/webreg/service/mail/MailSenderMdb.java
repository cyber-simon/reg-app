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

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;

import edu.kit.scc.webreg.exc.MailServiceException;

/**
 * Message-Driven Bean implementation class for: MailSenderMdb
 */
@MessageDriven(
		activationConfig = { 
				@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
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
     						mail.getBcc(), mail.getSubject(), mail.getBody());
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
