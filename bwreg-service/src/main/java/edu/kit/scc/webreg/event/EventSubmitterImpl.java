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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.Session;

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

	@Override
	public EventExecutor<AbstractEvent<? extends Serializable>, ?> resolveClass(String className)
			throws EventSubmitException {

		try {
			Object o = Class.forName(className).getConstructor().newInstance();
			if (o instanceof EventExecutor<?, ?>) {
				
				@SuppressWarnings("unchecked")
				EventExecutor<AbstractEvent<? extends Serializable>, ?> eventExecutor = 
					(EventExecutor<AbstractEvent<? extends Serializable>, ?>) o;
				
				return eventExecutor;
			}
			else {
				logger.warn("Could not execute job {}: not instance of EventExecutor", 
						className);
				throw new EventSubmitException("Failed spawning event executor");				
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			logger.warn("Failed spawning event executor", e);
			throw new EventSubmitException("Failed spawning event executor", e);
		}
	}
}
