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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.EventDao;
import edu.kit.scc.webreg.entity.EventEntity;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.exc.EventSubmitException;

@Stateless
public class EventSubmitterImpl implements EventSubmitter {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger; 
	
	@Inject
	private EventDao eventDao;
	
	@Resource(mappedName = "java:/JmsXA")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName = "java:/queue/bwIdmAsyncJobQueue")
	private Queue queue;

	@Override
	public void submit(AbstractEvent<? extends Serializable> event, List<EventEntity> eventList, EventType eventType, String executor) 
			throws EventSubmitException {

		for (EventEntity eventEntity : eventList) {
			if (eventType.equals(eventEntity.getEventType())) {
				try {
					Object o = Class.forName(eventEntity.getJobClass().getJobClassName()).newInstance();
					if (o instanceof EventExecutor<?, ?>) {
						
						@SuppressWarnings("unchecked")
						EventExecutor<AbstractEvent<? extends Serializable>, ?> eventExecutor = 
							(EventExecutor<AbstractEvent<? extends Serializable>, ?>) o;
						
						Map<String, String> jobStore;
						if (eventEntity.getJobClass().getJobStore() == null) {
							jobStore = new HashMap<String, String>();
						}
						else {
							jobStore = new HashMap<String, String>(eventEntity.getJobClass().getJobStore());
						}
							
						jobStore.put("executor", executor);
						
						eventExecutor.setJobStore(jobStore);
						eventExecutor.setEvent(event);
						submit(eventExecutor);
					}
					else {
						logger.warn("Could not execute job {} ({}): not instance of EventExecutor", 
								eventEntity.getJobClass().getName(), eventEntity.getJobClass().getJobClassName());
					}
				} catch (InstantiationException e) {
					logger.warn("Failed spawning event executor", e);
					throw new EventSubmitException("Failed spawning event executor", e);
				} catch (IllegalAccessException e) {
					logger.warn("Failed spawning event executor", e);
					throw new EventSubmitException("Failed spawning event executor", e);
				} catch (ClassNotFoundException e) {
					logger.warn("Failed spawning event executor", e);
					throw new EventSubmitException("Failed spawning event executor", e);
				}
			}
		}
		
	}	
	
	@Override
	public void submit(AbstractEvent<? extends Serializable> event, EventType eventType, String executor) 
			throws EventSubmitException {
		List<EventEntity> eventList = eventDao.findAllByEventType(eventType);

		submit(event, eventList, eventType, executor);
	}
	
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
