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

import java.util.HashSet;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.LocalGroupDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.service.LocalGroupService;
import edu.kit.scc.webreg.service.group.LocalGroupCreator;

@Stateless
public class LocalGroupServiceImpl extends BaseServiceImpl<LocalGroupEntity, Long> implements LocalGroupService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private LocalGroupDao dao;
	
	@Inject
	private LocalGroupCreator creator;
	
	@Inject
	private ServiceGroupFlagDao groupFlagDao;
	
	@Inject 
	private EventSubmitter eventSubmitter;

	@Override
	public LocalGroupEntity createNew(ServiceEntity service) {
		return creator.createNew(service);
	}
	
	@Override
	public void createServiceGroupFlagsBulk(ServiceEntity fromService, ServiceEntity toService, String filterRegex) {
		List<ServiceGroupFlagEntity> fromFlagList = groupFlagDao.findLocalGroupsForService(fromService);
		
		HashSet<GroupEntity> allChangedGroups = new HashSet<GroupEntity>();
		
		for (ServiceGroupFlagEntity fromFlag : fromFlagList) {
			if (fromFlag.getGroup() instanceof LocalGroupEntity) {
				LocalGroupEntity group = (LocalGroupEntity) fromFlag.getGroup();
				
				if (group.getName().matches(filterRegex)) {
					List<ServiceGroupFlagEntity> toFlagList = groupFlagDao.findByGroupAndService(group, toService);
					
					if (toFlagList.size() == 0) {
						logger.info("Creating group flags for group {} and service {}", group.getName(), toService.getShortName());
						ServiceGroupFlagEntity groupFlag = groupFlagDao.createNew();
						groupFlag.setService(toService);
						groupFlag.setGroup(group);
						groupFlag.setStatus(ServiceGroupStatus.DIRTY);
						
						groupFlag = groupFlagDao.persist(groupFlag);
						allChangedGroups.add(group);
					}
					else {
						logger.info("Skipping group flags for group {} and service {}, they already exist", group.getName(), toService.getShortName());
					}
				}
				else {
					logger.info("Skipping group {}. Doesn't match regex {}", group.getName(), filterRegex);
				}
			}
		}

		MultipleGroupEvent mge = new MultipleGroupEvent(allChangedGroups);
		try {
			eventSubmitter.submit(mge, EventType.GROUP_UPDATE, "bulk-job");
		} catch (EventSubmitException e) {
			logger.warn("Exeption", e);
		}
	}
	
	@Override
	public LocalGroupEntity save(LocalGroupEntity entity, ServiceEntity service) {
		return creator.save(entity, service);
	}
	
	@Override
	public LocalGroupEntity findWithUsers(Long id) {
		return dao.findWithUsers(id);
	}	
	
	@Override
	public LocalGroupEntity findWithUsersAndChildren(Long id) {
		return dao.findWithUsersAndChildren(id);
	}	
	
	@Override
	public LocalGroupEntity findByName(String name) {
		return dao.findByName(name);
	}	
	
	@Override
	public List<LocalGroupEntity> findByUser(UserEntity user) {
		return dao.findByUser(user);
	}	
	
	@Override
	protected BaseDao<LocalGroupEntity, Long> getDao() {
		return dao;
	}	
}
