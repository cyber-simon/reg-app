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
package edu.kit.scc.webreg.dao;

import java.util.List;

import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.GroupAdminRoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.SshPubKeyApproverRoleEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminRoleEntity;

public interface ServiceDao extends BaseDao<ServiceEntity> {

	ServiceEntity findByIdWithServiceProps(Long id);

	List<ServiceEntity> findByAdminRole(AdminRoleEntity role);

	List<ServiceEntity> findByApproverRole(ApproverRoleEntity role);

	ServiceEntity findByShortName(String shortName);

	List<ServiceEntity> findAllPublishedWithServiceProps();

	List<ServiceEntity> findByHotlineRole(AdminRoleEntity role);

	List<ServiceEntity> findByGroupCapability(Boolean capable);

	List<ServiceEntity> findByGroupAdminRole(GroupAdminRoleEntity role);

	List<ServiceEntity> findByParentService(ServiceEntity service);

	List<ServiceEntity> findBySshPubKeyApproverRole(SshPubKeyApproverRoleEntity role);

	List<ServiceEntity> findByProjectAdminRole(ProjectAdminRoleEntity role);

}
