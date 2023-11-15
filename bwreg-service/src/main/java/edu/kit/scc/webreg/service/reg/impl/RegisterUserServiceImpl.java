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
package edu.kit.scc.webreg.service.reg.impl;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.notEqual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity_;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class RegisterUserServiceImpl implements RegisterUserService {

	@Inject
	private Logger logger;

	@Inject
	private UserDao userDao;

	@Inject
	private GroupDao groupDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private Registrator registrator;

	@Inject
	private ServiceGroupFlagDao groupFlagDao;

	@Inject
	private UserTransaction userTransaction;

	@Override
	@RetryTransaction
	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, List<Long> policiesIdList,
			String executor) throws RegisterException {
		user = userDao.fetch(user.getId());
		service = serviceDao.fetch(service.getId());
		return registrator.registerUser(user, service, executor, policiesIdList, true, null);
	}

	@Override
	@RetryTransaction
	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor)
			throws RegisterException {
		user = userDao.fetch(user.getId());
		service = serviceDao.fetch(service.getId());
		return registrator.registerUser(user, service, executor, true);
	}

	@Override
	@RetryTransaction
	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor, Boolean sendGroupUpdate)
			throws RegisterException {
		user = userDao.fetch(user.getId());
		service = serviceDao.fetch(service.getId());
		return registrator.registerUser(user, service, executor, null, sendGroupUpdate, null);
	}

	@Override
	@RetryTransaction
	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor, Boolean sendGroupUpdate,
			Auditor parentAuditor) throws RegisterException {
		user = userDao.fetch(user.getId());
		service = serviceDao.fetch(service.getId());
		return registrator.registerUser(user, service, executor, null, sendGroupUpdate, parentAuditor);
	}

	@Override
	public List<RegistryEntity> updateGroupsNew(Set<GroupEntity> groupUpdateSet, Boolean reconRegistries,
			Set<String> reconRegForServices, Boolean fullRecon, Boolean newRollMech,
			Map<GroupEntity, Set<UserEntity>> usersToRemove, String executor) throws RegisterException {
		List<RegistryEntity> reconList = new ArrayList<RegistryEntity>();

		logger.debug("Starting new updateGroups method for groupUpdateSet size {}, usersToRemove size {}",
				groupUpdateSet.size(), (usersToRemove != null ? usersToRemove.size() : "(not set)"));

		// randomize the order of the groups. It seem that multiple threads work in the
		// same order,
		// thus always working on the same groups, which is inefficient and causes
		// optimistic locks
		List<GroupEntity> groupUpdateList = new ArrayList<GroupEntity>(groupUpdateSet);
		Collections.shuffle(groupUpdateList);

		for (GroupEntity group : groupUpdateList) {
			if (group instanceof ServiceBasedGroupEntity) {
				ServiceBasedGroupEntity serviceBasedGroupEntity = (ServiceBasedGroupEntity) group;
				List<ServiceGroupFlagEntity> flagList = groupFlagDao
						.findAll(and(equal(ServiceGroupFlagEntity_.group, serviceBasedGroupEntity),
								notEqual(ServiceGroupFlagEntity_.status, ServiceGroupStatus.CLEAN)));
				for (ServiceGroupFlagEntity flag : flagList) {
					Boolean changed = registrator.processUpdateGroup(flag, reconRegistries, fullRecon, newRollMech, executor);

					if (changed && reconRegistries && !(serviceBasedGroupEntity instanceof HomeOrgGroupEntity)) {
						if (reconRegForServices == null
								|| reconRegForServices.contains(flag.getService().getShortName())) {
							List<UserEntity> userList = groupDao.getUsersOfGroup(flag.getGroup().getId());
							List<RegistryEntity> registryList = registryDao.findByServiceAndStatus(flag.getService(),
									RegistryStatus.ACTIVE);
							for (RegistryEntity registry : registryList) {
								if (userList.contains(registry.getUser())) {
									reconList.add(registry);
								}
								if (usersToRemove != null && usersToRemove.containsKey(flag.getGroup())
										&& usersToRemove.get(flag.getGroup()).contains(registry.getUser())) {
									reconList.add(registry);
								}
							}
						}
					}
				}
			}
		}

		logger.debug("Done new updateGroups method. ReconList size is {}", reconList.size());

		return reconList;
	}

	@Override
	public void updateGroups(Set<GroupEntity> groupUpdateSet, Boolean reconRegistries, Boolean fullRecon,
			Map<GroupEntity, Set<UserEntity>> usersToRemove, String executor) throws RegisterException {
		GroupPerServiceList groupUpdateList = registrator.buildGroupPerServiceList(groupUpdateSet, reconRegistries,
				fullRecon, executor);
		for (ServiceEntity service : groupUpdateList.getServices()) {
			registrator.updateGroups(service, groupUpdateList, reconRegistries, fullRecon, usersToRemove, executor);
		}
	}

	@Override
	@RetryTransaction
	public void deleteGroup(GroupEntity group, ServiceEntity service, String executor) throws RegisterException {
		registrator.deleteGroup(group, service, executor);
	}

	@Override
	@RetryTransaction
	public void reconsiliation(RegistryEntity registry, Boolean fullRecon, String executor) throws RegisterException {
		registrator.reconsiliation(registry, fullRecon, executor, null);
	}

	@Override
	@RetryTransaction
	public void reconsiliation(RegistryEntity registry, Boolean fullRecon, String executor, Auditor parentAuditor)
			throws RegisterException {
		registrator.reconsiliation(registry, fullRecon, executor, parentAuditor);
	}

	@Override
	@RetryTransaction
	public void deregisterUser(RegistryEntity registry, String executor, String statusMessage)
			throws RegisterException {
		registrator.deregisterUser(registry, executor, statusMessage);
	}

	@Override
	public void deregisterUser(RegistryEntity registry, String executor, Auditor parentAuditor, String statusMessage)
			throws RegisterException {
		registrator.deregisterUser(registry, executor, parentAuditor, statusMessage);
	}

	@Override
	@RetryTransaction
	public void setPassword(UserEntity user, ServiceEntity service, RegistryEntity registry, String password,
			String executor) throws RegisterException {
		registry = registryDao.fetch(registry.getId());
		registrator.setPassword(user, service, registry, password, executor);
	}

	@Override
	@RetryTransaction
	public void deletePassword(UserEntity user, ServiceEntity service, RegistryEntity registry, String executor)
			throws RegisterException {
		registry = registryDao.fetch(registry.getId());
		registrator.deletePassword(user, service, registry, executor);
	}

	@Override
	public Boolean checkWorkflow(String name) {
		return registrator.checkWorkflow(name);
	}

	@Override
	public RegisterUserWorkflow getWorkflowInstance(String className) {
		return registrator.getWorkflowInstance(className);
	}

	@Override
	@RetryTransaction
	public void reconGroupsForRegistry(RegistryEntity registry, String executor) throws RegisterException {
		registry = registryDao.fetch(registry.getId());
		registrator.reconGroupsForRegistry(registry, executor);
	}

	@Override
	@Asynchronous
	@RetryTransaction
	public void completeReconciliation(ServiceEntity service, Boolean fullRecon, Boolean withGroups, Boolean onlyActive,
			String executor) {
		registrator.completeReconciliation(service, fullRecon, withGroups, onlyActive, executor);
	}

	@Override
	@RetryTransaction
	public void completeReconciliationForRegistry(ServiceEntity service, RegistryEntity registry, Boolean fullRecon,
			Boolean withGroups, String executor) throws RegisterException {
		registrator.completeReconciliationForRegistry(service, registry, fullRecon, withGroups, executor);
	}

	@Override
	@RetryTransaction
	public void deprovision(RegistryEntity registry, String executor) throws RegisterException {
		registrator.deprovision(registry, executor);
	}

	@Override
	@RetryTransaction
	public void purge(RegistryEntity registry, String executor) throws RegisterException {
		registrator.purge(registry, executor);
	}
}
