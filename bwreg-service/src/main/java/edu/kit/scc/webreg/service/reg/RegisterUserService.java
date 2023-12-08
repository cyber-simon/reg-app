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
package edu.kit.scc.webreg.service.reg;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface RegisterUserService {

	Boolean checkWorkflow(String name);

	RegisterUserWorkflow getWorkflowInstance(String className);

	RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor) throws RegisterException;

	RegistryEntity registerUser(UserEntity user, ServiceEntity service, List<Long> policiesIdList, String executor)
			throws RegisterException;

	void reconsiliation(RegistryEntity registry, Boolean fullRecon, String executor, Auditor parentAuditor)
			throws RegisterException;

	void deregisterUser(RegistryEntity registry, String executor, Auditor parentAuditor, String statusMessage)
			throws RegisterException;

	void deregisterUser(RegistryEntity registry, String executor, String statusMessage) throws RegisterException;

	void deprovision(RegistryEntity registry, String executor) throws RegisterException;

	void purge(RegistryEntity registry, String executor) throws RegisterException;

	void setPassword(UserEntity user, ServiceEntity service, RegistryEntity registry, String password, String executor)
			throws RegisterException;

	void deleteGroup(GroupEntity group, ServiceEntity service, String executor) throws RegisterException;

	void deletePassword(UserEntity user, ServiceEntity service, RegistryEntity registry, String executor)
			throws RegisterException;

	void reconGroupsForRegistry(RegistryEntity registry, String executor) throws RegisterException;

	RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor, Boolean sendGroupUpdate)
			throws RegisterException;

	void reconsiliation(RegistryEntity registry, Boolean fullRecon, String executor) throws RegisterException;

	RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor, Boolean sendGroupUpdate,
			Auditor parentAuditor) throws RegisterException;

	void completeReconciliation(ServiceEntity service, Boolean fullRecon, Boolean withGroups, Boolean onlyActive,
			String executor);

	void completeReconciliationForRegistry(ServiceEntity service, RegistryEntity registry, Boolean fullRecon,
			Boolean withGroups, String executor) throws RegisterException;

	List<RegistryEntity> updateGroupsNew(Set<GroupEntity> groupUpdateSet, Boolean reconRegistries,
			Set<String> reconRegForServices, Boolean fullRecon, Map<GroupEntity, Set<UserEntity>> usersToRemove,
			String executor) throws RegisterException;
}
