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
package edu.kit.scc.webreg.dao.jpa;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.GroupAdminRoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceEntity_;
import edu.kit.scc.webreg.entity.SshPubKeyApproverRoleEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminRoleEntity;

@Named
@ApplicationScoped
public class JpaServiceDao extends JpaBaseDao<ServiceEntity> implements ServiceDao {

	@Override
	public ServiceEntity findByShortName(String shortName) {
		return find(equal(ServiceEntity_.shortName, shortName));
	}

	@Override
	public List<ServiceEntity> findAllPublishedWithServiceProps() {
		return findAll(equal(ServiceEntity_.published, Boolean.TRUE), ServiceEntity_.serviceProps);
	}

	@Override
	public List<ServiceEntity> findByParentService(ServiceEntity service) {
		return findAll(equal(ServiceEntity_.parentService, service));
	}

	@Override
	public List<ServiceEntity> findByAdminRole(AdminRoleEntity role) {
		return findAll(equal(ServiceEntity_.adminRole, role));
	}

	@Override
	public List<ServiceEntity> findByHotlineRole(AdminRoleEntity role) {
		return findAll(equal(ServiceEntity_.hotlineRole, role));
	}

	@Override
	public List<ServiceEntity> findByApproverRole(ApproverRoleEntity role) {
		return findAll(equal(ServiceEntity_.approverRole, role));
	}

	@Override
	public List<ServiceEntity> findBySshPubKeyApproverRole(SshPubKeyApproverRoleEntity role) {
		return findAll(equal(ServiceEntity_.sshPubKeyApproverRole, role));
	}

	@Override
	public List<ServiceEntity> findByGroupAdminRole(GroupAdminRoleEntity role) {
		return findAll(equal(ServiceEntity_.groupAdminRole, role));
	}

	@Override
	public List<ServiceEntity> findByProjectAdminRole(ProjectAdminRoleEntity role) {
		return findAll(equal(ServiceEntity_.projectAdminRole, role));
	}

	@Override
	public List<ServiceEntity> findByGroupCapability(Boolean capable) {
		return findAll(equal(ServiceEntity_.groupCapable, capable));
	}

	@Override
	public ServiceEntity findByIdWithServiceProps(Long id) {
		return find(equal(ServiceEntity_.id, id), ServiceEntity_.serviceProps, ServiceEntity_.policies,
				ServiceEntity_.projectPolicies);
	}

	@Override
	public Class<ServiceEntity> getEntityClass() {
		return ServiceEntity.class;
	}

}
