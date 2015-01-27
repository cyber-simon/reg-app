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
import java.util.List;

import edu.kit.scc.webreg.entity.EventEntity;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.exc.EventSubmitException;

public interface EventSubmitter extends Serializable {

	void submit(EventExecutor<?, ?> eventExecutor)
			throws EventSubmitException;

	void submit(AbstractEvent<? extends Serializable> event,
			EventType eventType, String executor) throws EventSubmitException;

	void submit(AbstractEvent<? extends Serializable> event,
			List<EventEntity> eventList, EventType eventType, String executor)
			throws EventSubmitException;

}
