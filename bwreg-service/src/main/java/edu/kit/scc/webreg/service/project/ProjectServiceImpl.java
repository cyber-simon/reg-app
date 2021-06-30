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
package edu.kit.scc.webreg.service.project;

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
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class ProjectServiceImpl extends BaseServiceImpl<ProjectEntity, Long> implements ProjectService {

	private static final long serialVersionUID = 1L;

	@Inject
	private IdentityDao identityDao;
	
	@Inject
	private ProjectDao dao;
	
	@Inject
	private ProjectUpdater updater;
	
	@Override
	public void updateProjectMemberList(ProjectEntity project, Set<IdentityEntity> memberList, String executor) {
		updater.updateProjectMemberList(project, memberList, executor);
	}
	
	@Override
	public void updateServices(ProjectEntity project, Set<ServiceEntity> services, String executor) {
		updater.updateServices(project, services, executor);
	}
	
	@Override
	public List<ProjectEntity> findByService(ServiceEntity service) {
		return dao.findByService(service);
	}

	@Override
	public List<ProjectIdentityAdminEntity> findAdminByUserId(Long identityId) {
		IdentityEntity identity = identityDao.findById(identityId);
		return dao.findAdminByIdentity(identity);
	}

	@Override
	public List<ProjectMembershipEntity> findByIdentity(IdentityEntity identity) {
		return dao.findByIdentity(identity);
	}
	
	@Override
	public List<ProjectIdentityAdminEntity> findAdminsForProject(ProjectEntity project) {
		return dao.findAdminsForProject(project);
	}

	@Override
	public List<ProjectMembershipEntity> findMembersForProject(ProjectEntity project) {
		return dao.findMembersForProject(project);
	}

	@Override
	public List<ProjectServiceEntity> findServicesForProject(ProjectEntity project) {
		return dao.findServicesForProject(project);
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
		IdentityEntity identity = identityDao.findById(identityId);
		
		project = dao.persist(project);
		dao.addAdminToProject(project, identity, ProjectAdminType.OWNER);
		return project;
	}
		
	@Override
	protected BaseDao<ProjectEntity, Long> getDao() {
		return dao;
	}
}
