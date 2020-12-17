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
import java.util.Set;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;

@Stateless
public class RegisterUserServiceImpl implements RegisterUserService {

	@Inject
	private Registrator registrator;

	@Override
	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, List<Long> policiesIdList, String executor)
			throws RegisterException {
		return registrator.registerUser(user, service, executor, policiesIdList, true, null);
	}

	@Override
	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor)
			throws RegisterException {
		return registrator.registerUser(user, service, executor, true);
	}

	@Override
	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor, Boolean sendGroupUpdate)
			throws RegisterException {
		return registrator.registerUser(user, service, executor, null, sendGroupUpdate, null);
	}

	@Override
	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor, Boolean sendGroupUpdate, Auditor parentAuditor)
			throws RegisterException {
		return registrator.registerUser(user, service, executor, null, sendGroupUpdate, parentAuditor);
	}
	
	@Override
	public void updateGroups(Set<GroupEntity> groupUpdateSet, String executor) throws RegisterException {
		registrator.updateGroups(groupUpdateSet, executor);
	}
	
	@Override
	public void deleteGroup(GroupEntity group, ServiceEntity service, String executor) throws RegisterException {
		registrator.deleteGroup(group, service, executor);
	}

	@Override
	public void reconsiliation(RegistryEntity registry, Boolean fullRecon, String executor) throws RegisterException {
		registrator.reconsiliation(registry, fullRecon, executor, null);
	}
	
	@Override
	public void reconsiliation(RegistryEntity registry, Boolean fullRecon, String executor, Auditor parentAuditor) throws RegisterException {
		registrator.reconsiliation(registry, fullRecon, executor, parentAuditor);
	}

	@Override
	public void deregisterUser(RegistryEntity registry, String executor) throws RegisterException {
		registrator.deregisterUser(registry, executor, null);
	}
	
	@Override
	public void deregisterUser(RegistryEntity registry, String executor, Auditor parentAuditor) throws RegisterException {
		registrator.deregisterUser(registry, executor, parentAuditor);
	}
	
	@Override
	public void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, String password, String executor) throws RegisterException {
		registrator.setPassword(user, service, registry, password, executor);
	}
	
	@Override
	public void deletePassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, String executor) throws RegisterException {
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
	public void reconGroupsForRegistry(RegistryEntity registry, String executor) throws RegisterException {
		registrator.reconGroupsForRegistry(registry, executor);
	}
	
	@Override
	@Asynchronous
	public void completeReconciliation(ServiceEntity service, Boolean fullRecon, Boolean withGroups, 
			Boolean onlyActive, String executor) {
		registrator.completeReconciliation(service, fullRecon, withGroups, onlyActive, executor);
	}

	@Override
	public void deprovision(RegistryEntity registry, String executor) throws RegisterException {
		registrator.deprovision(registry, executor);
	}

	@Override
	public void purge(RegistryEntity registry, String executor) throws RegisterException {
		registrator.purge(registry, executor);
	}
}
