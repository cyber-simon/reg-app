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

	void registerUser(UserEntity user, ServiceEntity service, String executor)
			throws RegisterException;

	void reconsiliation(RegistryEntity registry, Boolean fullRecon,
			String executor, Auditor parentAuditor) throws RegisterException;

	void deregisterUser(RegistryEntity registry, String executor)
			throws RegisterException;

	void deprovision(RegistryEntity registry, String executor) throws RegisterException;
	
	void purge(RegistryEntity registry, String executor) throws RegisterException;
	
	void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, String password, String executor)
			throws RegisterException;

	void deleteGroup(GroupEntity group, ServiceEntity service, String executor)
			throws RegisterException;

	void completeReconciliation(ServiceEntity service, Boolean fullRecon,
			Boolean withGroups, String executor);

	void updateGroups(Set<GroupEntity> groupUpdateSet, String executor)
			throws RegisterException;

	void deletePassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, String executor) throws RegisterException;

	void reconGroupsForRegistry(RegistryEntity registry, String executor)
			throws RegisterException;

	void registerUser(UserEntity user, ServiceEntity service, String executor,
			Boolean sendGroupUpdate) throws RegisterException;

	void reconsiliation(RegistryEntity registry, Boolean fullRecon,
			String executor) throws RegisterException;

	void registerUser(UserEntity user, ServiceEntity service, String executor,
			Boolean sendGroupUpdate, Auditor parentAuditor)
			throws RegisterException;
	
}
