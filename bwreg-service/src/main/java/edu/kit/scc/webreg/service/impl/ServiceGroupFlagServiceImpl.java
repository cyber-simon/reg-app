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
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;

@Stateless
public class ServiceGroupFlagServiceImpl extends BaseServiceImpl<ServiceGroupFlagEntity, Long> implements ServiceGroupFlagService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceGroupFlagDao dao;
	
	@Override
	public List<ServiceGroupFlagEntity> findByGroup(ServiceBasedGroupEntity group) {
		return dao.findByGroup(group);
	}

	@Override
	public List<ServiceGroupFlagEntity> findByGroupAndStatus(ServiceBasedGroupEntity group, ServiceGroupStatus status) {
		return dao.findByGroupAndStatus(group, status);
	}

	@Override
	public List<ServiceGroupFlagEntity> findByStatus(ServiceGroupStatus status) {
		return dao.findByStatus(status);
	}

	@Override
	public List<ServiceGroupFlagEntity> findByService(ServiceEntity service) {
		return dao.findByService(service);
	}

	@Override
	public List<ServiceGroupFlagEntity> findLocalGroupsForService(ServiceEntity service) {
		return dao.findLocalGroupsForService(service);
	}
	
	@Override
	public List<ServiceGroupFlagEntity> findByGroupAndService(ServiceBasedGroupEntity group, ServiceEntity service) {
		return dao.findByGroupAndService(group, service);
	}
	
	@Override
	protected BaseDao<ServiceGroupFlagEntity, Long> getDao() {
		return dao;
	}
}
