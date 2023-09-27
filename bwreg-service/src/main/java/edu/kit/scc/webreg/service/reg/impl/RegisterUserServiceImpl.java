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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class RegisterUserServiceImpl implements RegisterUserService {

	@Inject
	private UserDao userDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private Registrator registrator;

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
	public void updateGroups(Set<GroupEntity> groupUpdateSet, Boolean reconRegistries, Boolean fullRecon,
			Map<GroupEntity, Set<UserEntity>> usersToRemove, String executor) throws RegisterException {
		GroupPerServiceList groupUpdateList = registrator.buildGroupPerServiceList(groupUpdateSet, reconRegistries, fullRecon, executor);
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
