/*
 * *****************************************************************************
 * Copyright (c) 2014 Michael Simon.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Michael Simon - initial
 * *****************************************************************************
 */
package edu.kit.scc.webreg.service.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceStatusType;
import edu.kit.scc.webreg.entity.project.ProjectServiceType;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class ProjectServiceImpl extends BaseServiceImpl<ProjectEntity> implements ProjectService {

	private static final long serialVersionUID = 1L;

	@Inject
	private IdentityDao identityDao;

	@Inject
	private ProjectDao dao;

	@Inject
	private LocalProjectUpdater updater;

	@Override
	public void updateProjectMemberList(ProjectEntity project, Set<IdentityEntity> memberList, String executor) {
		project = dao.merge(project);
		updater.updateProjectMemberList(project, memberList, executor);
	}

	@Override
	public void addProjectMember(ProjectEntity project, IdentityEntity identity, String executor) {
		project = dao.merge(project);
		updater.addProjectMember(project, identity, executor);
	}

	@Override
	public void removeProjectMember(ProjectMembershipEntity pme, String executor) {
		updater.removeProjectMember(pme, executor);
	}

	@Override
	public void updateServices(ProjectEntity project, Set<ServiceEntity> services, ProjectServiceType type, ProjectServiceStatusType status,
			String executor) {
		updater.updateServices(project, services, type, status, executor);
	}

	@Override
	public void addOrChangeService(ProjectEntity project, ServiceEntity service, ProjectServiceType type, ProjectServiceStatusType status,
			String executor) {
		updater.addOrChangeService(project, service, type, status, executor);
	}

	@Override
	public List<ProjectServiceEntity> findAllByService(ServiceEntity service) {
		return dao.findAllByService(service);
	}

	@Override
	public List<ProjectIdentityAdminEntity> findAdminByUserId(Long identityId) {
		IdentityEntity identity = identityDao.fetch(identityId);
		return dao.findAdminByIdentity(identity);
	}

	@Override
	public List<ProjectMembershipEntity> findByIdentity(IdentityEntity identity) {
		return dao.findByIdentity(identity);
	}

	@Override
	public ProjectMembershipEntity findByIdentityAndProject(IdentityEntity identity, ProjectEntity project) {
		return dao.findByIdentityAndProject(identity, project);
	}

	@Override
	public List<ProjectIdentityAdminEntity> findAdminsForProject(ProjectEntity project) {
		return dao.findAdminsForProject(project);
	}

	@Override
	public List<ProjectMembershipEntity> findMembersForProject(ProjectEntity project) {
		return dao.findMembersForProject(project, false);
	}

	@Override
	public List<ProjectMembershipEntity> findMembersForProject(ProjectEntity project, boolean withChildren) {
		return dao.findMembersForProject(project, withChildren);
	}

	@Override
	public List<ProjectServiceEntity> findServicesForProject(ProjectEntity project) {
		return new ArrayList<>(dao.findServicesForProject(project, false));
	}

	@Override
	public List<ProjectServiceEntity> findServicesFromParentsForProject(ProjectEntity project) {
		if (project.getParentProject() != null) {
			return new ArrayList<>(dao.findServicesForProject(project.getParentProject(), true));
		} else {
			return null;
		}
	}

	@Override
	public void addAdminToProject(ProjectEntity project, IdentityEntity identity, ProjectAdminType type, String executor) {
		dao.addAdminToProject(project, identity, type);
	}

	@Override
	public void removeAdminFromProject(ProjectIdentityAdminEntity pia, String executor) {
		dao.removeAdminFromProject(pia);
	}

	@Override
	public ProjectEntity save(ProjectEntity project, Long identityId) {
		IdentityEntity identity = identityDao.fetch(identityId);

		project = dao.persist(project);
		dao.addAdminToProject(project, identity, ProjectAdminType.OWNER);
		return project;
	}

	@Override
	protected BaseDao<ProjectEntity> getDao() {
		return dao;
	}

}
