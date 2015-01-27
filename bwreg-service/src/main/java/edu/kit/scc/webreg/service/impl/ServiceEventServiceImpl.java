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
import edu.kit.scc.webreg.dao.ServiceEventDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceEventEntity;
import edu.kit.scc.webreg.service.ServiceEventService;

@Stateless
public class ServiceEventServiceImpl extends BaseServiceImpl<ServiceEventEntity, Long> implements ServiceEventService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceEventDao dao;
	
	@Override
	protected BaseDao<ServiceEventEntity, Long> getDao() {
		return dao;
	}
	
	@Override
	public List<ServiceEventEntity> findAllByService(ServiceEntity service) {
		return dao.findAllByService(service);
	}
}
