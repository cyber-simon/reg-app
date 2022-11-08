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
package edu.kit.scc.webreg.event;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import edu.kit.scc.webreg.event.exc.EventSubmitException;

@Stateless
public class EventSubmitterImpl extends AbstractEventSubmitterImpl implements EventSubmitter {

	private static final long serialVersionUID = 1L;
	
	@Resource(mappedName = "java:/JmsXA")
	ConnectionFactory connectionFactory;

	@Resource(mappedName = "java:/queue/bwIdmAsyncJobQueue")
	Queue queue;

	@Override
	public void submit(EventExecutor<?, ?> eventExecutor) 
			throws EventSubmitException {
		Connection connection = null;
		Session session = null;
		MessageProducer messageProducer = null;
		
		try {

			logger.debug("Opening JMS connection");
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			messageProducer = session.createProducer(queue);

			ObjectMessage message = session.createObjectMessage();
			message.setObject(eventExecutor);
			messageProducer.send(message);

			logger.debug("EventExecutor message sent");
			
		} catch (JMSException e) {
			logger.warn("Could not send JMS Message", e);
			throw new EventSubmitException("Could not send JMS Message", e);
		} finally {
			try {
				if (messageProducer != null)
					messageProducer.close();
				
				if (session != null)
					session.close();
	
				if (connection != null)
					connection.close();
			} catch (JMSException inner) {
				logger.warn("Could not close JMS: " + inner);
			}
			
		}
		
	}
}
