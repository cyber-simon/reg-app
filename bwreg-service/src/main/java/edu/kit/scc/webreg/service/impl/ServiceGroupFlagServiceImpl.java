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

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;

@Stateless
public class ServiceGroupFlagServiceImpl extends BaseServiceImpl<ServiceGroupFlagEntity>
		implements ServiceGroupFlagService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private ServiceGroupFlagDao dao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private GroupDao groupDao;

	@Override
	public void createFlagIfMissing(ServiceBasedGroupEntity serviceBasedGroup, ServiceEntity serviceEntity) {
		if (!serviceEntity.getGroupCapable()) {
			logger.warn(
					"Do not call create service flag on a service that is not capable of groups (Group {}, Service {})",
					serviceBasedGroup.getName(), serviceEntity.getName());
			return;
		}

		serviceBasedGroup = (ServiceBasedGroupEntity) groupDao.fetch(serviceBasedGroup.getId());
		serviceEntity = serviceDao.fetch(serviceEntity.getId());
		
		List<ServiceGroupFlagEntity> flagList = dao.findByGroupAndService(serviceBasedGroup, serviceEntity);
		if (flagList.size() > 0) {
			logger.debug("ServiceGroupFlag for service {} and group {} exists", serviceEntity.getName(),
					serviceBasedGroup.getName());
		} else {
			logger.debug("Create for service {} and group {}", serviceEntity.getName(), serviceBasedGroup.getName());
			ServiceGroupFlagEntity flag = dao.createNew();
			flag.setGroup(serviceBasedGroup);
			flag.setService(serviceEntity);
			flag.setStatus(ServiceGroupStatus.DIRTY);
			flag = dao.persist(flag);
		}
	}

	@Override
	public List<ServiceGroupFlagEntity> findByGroup(ServiceBasedGroupEntity group) {
		group = (ServiceBasedGroupEntity) groupDao.fetch(group.getId());
		return dao.findByGroup(group);
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
	protected BaseDao<ServiceGroupFlagEntity> getDao() {
		return dao;
	}
}
