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

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;

import edu.kit.scc.webreg.job.ExecutableJob;

@MessageDriven(
		activationConfig = { 
				@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
				@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/bwIdmAsyncJobQueue")
		})
public class AsyncJobMdb implements MessageListener {
	
	@Inject
	private Logger logger;
	
    public void onMessage(Message message) {
    	if (message instanceof ObjectMessage) {
			ObjectMessage om = (ObjectMessage) message;

			try {
				Object o = om.getObject();
     			if (o instanceof ExecutableJob) {
    				ExecutableJob job = (ExecutableJob) o;
    				job.execute();
     			}
     			else if (o instanceof EventExecutor<?, ?>) {
     				EventExecutor<?, ?> eventExecutor = (EventExecutor<?, ?>) o;
     				eventExecutor.execute();
     			}
     	    	else {
     	    		logger.warn("Nothing to do for class {}. Ignore.", message.getClass());
     	    	}
     			
			} catch (JMSException e) {
				logger.warn("JMS Exception happened while trying to fetch Object", e);
			}
    	}
    	else {
    		logger.warn("Message not an object message. Ignore.");
    	}
    }

}
