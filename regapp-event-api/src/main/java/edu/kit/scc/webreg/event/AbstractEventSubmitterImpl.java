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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.EventDao;
import edu.kit.scc.webreg.dao.ops.RqlExpressions;
import edu.kit.scc.webreg.entity.EventEntity;
import edu.kit.scc.webreg.entity.EventEntity_;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.ServiceEventEntity;
import edu.kit.scc.webreg.event.exc.EventSubmitException;

public abstract class AbstractEventSubmitterImpl implements EventSubmitter {

	private static final long serialVersionUID = 1L;

	@Inject
	protected Logger logger;

	@Inject
	protected EventDao eventDao;

	@Override
	public void submit(AbstractEvent<? extends Serializable> event, List<EventEntity> eventList, EventType eventType,
			String executor) throws EventSubmitException {

		for (EventEntity eventEntity : eventList) {
			if (eventType.equals(eventEntity.getEventType())) {
				EventExecutor<AbstractEvent<? extends Serializable>, ?> eventExecutor = resolveClass(
						eventEntity.getJobClass().getJobClassName());

				Map<String, String> jobStore;
				if (eventEntity.getJobClass().getJobStore() == null) {
					jobStore = new HashMap<String, String>();
				} else {
					jobStore = new HashMap<String, String>(eventEntity.getJobClass().getJobStore());
				}

				jobStore.put("executor", executor);

				eventExecutor.setJobStore(jobStore);
				eventExecutor.setEvent(event);
				submit(eventExecutor);
			} else {
				logger.debug("EventType not matching type of EventEntity: {} <-> {}, ignoring",
						eventEntity.getEventType(), eventType);
			}
		}
	}

	@Override
	public void submit(AbstractEvent<? extends Serializable> event, EventType eventType, String executor)
			throws EventSubmitException {
		/*
		 * always filter serviceevents from events, if not explicitly specified
		 * otherwise
		 */
		submit(event, eventType, executor, true);
	}

	@Override
	public void submit(AbstractEvent<? extends Serializable> event, EventType eventType, String executor,
			Boolean filterServiceEvents) throws EventSubmitException {
		List<EventEntity> eventListData = findAllByEventType(eventType);
		List<EventEntity> eventList;

		if (filterServiceEvents) {
			eventList = new ArrayList<EventEntity>();
			for (EventEntity e : eventListData) {
				if (!(e instanceof ServiceEventEntity)) {
					eventList.add(e);
				}
			}
		} else {
			eventList = eventListData;
		}

		submit(event, eventList, eventType, executor);
	}

	private List<EventEntity> findAllByEventType(EventType eventType) {
		return eventDao.findAll(RqlExpressions.equal(EventEntity_.eventType, eventType));
	}

}
