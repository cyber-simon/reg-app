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
package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.UserEventDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.UserEventEntity;
import edu.kit.scc.webreg.service.UserEventService;

@Stateless
public class UserEventServiceImpl extends BaseServiceImpl<UserEventEntity, Long> implements UserEventService {

	private static final long serialVersionUID = 1L;

	@Inject
	private UserEventDao dao;
	
	@Override
	protected BaseDao<UserEventEntity, Long> getDao() {
		return dao;
	}
	
	@Override
	public List<UserEventEntity> findAllByService(EventType eventType) {
		return dao.findAllByEventType(eventType);
	}
}
