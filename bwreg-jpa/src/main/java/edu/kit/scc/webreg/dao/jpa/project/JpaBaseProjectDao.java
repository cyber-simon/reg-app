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
package edu.kit.scc.webreg.dao.jpa.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.project.BaseProjectDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipType;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceStatusType;
import edu.kit.scc.webreg.entity.project.ProjectServiceType;

public abstract class JpaBaseProjectDao<T extends ProjectEntity> extends JpaBaseDao<T> implements BaseProjectDao<T> {

	public abstract List<T> findByService(ServiceEntity service);
	
	@Override
	public ProjectServiceEntity mergeProjectService(ProjectServiceEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectServiceEntity> findAllByService(ServiceEntity service) {
		return em.createQuery("select r from ProjectServiceEntity r where r.service = :service order by r.project.name")
				.setParameter("service", service).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectIdentityAdminEntity> findAdminByIdentity(IdentityEntity identity) {
		return em.createQuery("select r from ProjectIdentityAdminEntity r where r.identity = :identity order by r.project.name")
				.setParameter("identity", identity).getResultList();
	}

	@Override
	public List<ProjectMembershipEntity> findMembersForProject(ProjectEntity project, boolean withChildren) {
		if (! em.contains(project))
			project = em.merge(project);

		List<ProjectMembershipEntity> membershipList = new ArrayList<ProjectMembershipEntity>();
		findMembersForProject(project, membershipList, withChildren, 0, 3);
		return membershipList;
	}
	
	@SuppressWarnings("unchecked")
	private void findMembersForProject(ProjectEntity project, List<ProjectMembershipEntity> membershipList, boolean withChildren, int depth, int maxDepth) {
		if (depth >= maxDepth) {
			return;
		}
		
		membershipList.addAll(em.createQuery("select r from ProjectMembershipEntity r where r.project = :project")
				.setParameter("project", project).getResultList());
		
		if (withChildren) {
			for (ProjectEntity childProject : project.getChildProjects()) {
				findMembersForProject(childProject, membershipList, true, depth + 1, maxDepth);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<IdentityEntity> findIdentitiesForProject(ProjectEntity project) {
		return em.createQuery("select r.identity from ProjectMembershipEntity r where r.project = :project")
				.setParameter("project", project).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectMembershipEntity> findByIdentity(IdentityEntity identity) {
		return em.createQuery("select r from ProjectMembershipEntity r where r.identity = :identity")
				.setParameter("identity", identity).getResultList();
	}

	@Override
	public ProjectMembershipEntity findByIdentityAndProject(IdentityEntity identity, ProjectEntity project) {
		try {
			return (ProjectMembershipEntity) em.createQuery("select r from ProjectMembershipEntity r where r.identity = :identity and r.project = :project")
				.setParameter("identity", identity).setParameter("project", project).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public ProjectServiceEntity findByServiceAndProject(ServiceEntity service, ProjectEntity project) {
		try {
			return (ProjectServiceEntity) em.createQuery("select r from ProjectServiceEntity r where r.service = :service and r.project = :project")
				.setParameter("service", service).setParameter("project", project).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectIdentityAdminEntity> findAdminsForProject(ProjectEntity project) {
		return em.createQuery("select r from ProjectIdentityAdminEntity r where r.project = :project")
				.setParameter("project", project).getResultList();
	}

	@Override
	public Set<ProjectServiceEntity> findServicesForProject(ProjectEntity project, Boolean withParents) {
		return findServicesForProject(project, withParents, null);
	}
	
	@Override
	public Set<ProjectServiceEntity> findServicesForProject(ProjectEntity project, Boolean withParents, ProjectServiceStatusType status) {
		Set<ProjectServiceEntity> resultList = new HashSet<ProjectServiceEntity>();
		
		if (withParents) {
			addServicesForProject(resultList, project, 0, 3, status);
		}
		else {
			addServicesForProject(resultList, project, 0, 1, status);
		}

		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	private void addServicesForProject(Set<ProjectServiceEntity> resultList, ProjectEntity project, int depth, int maxDepth, ProjectServiceStatusType status) {
		if (depth >= maxDepth) {
			return;
		}
		else {
			if (status != null) {
				resultList.addAll(em.createQuery("select r from ProjectServiceEntity r where r.project = :project and status = :status")
						.setParameter("project", project).setParameter("status", status)
						.getResultList());
			}
			else {
				// find all, if status is null
				resultList.addAll(em.createQuery("select r from ProjectServiceEntity r where r.project = :project")
						.setParameter("project", project)
						.getResultList());
			}
			
			if (project.getParentProject() != null) {
				addServicesForProject(resultList, project, depth + 1, maxDepth, status);
			}
		}
	}

	@Override
	public ProjectIdentityAdminEntity addAdminToProject(ProjectEntity project, IdentityEntity identity, ProjectAdminType type) {
		ProjectIdentityAdminEntity entity = new ProjectIdentityAdminEntity();
		entity.setProject(project);
		entity.setIdentity(identity);
		entity.setType(type);
		em.persist(entity);
		return entity;
	}

	@Override
	public void removeAdminFromProject(ProjectIdentityAdminEntity pia) {
		pia = em.merge(pia);
		em.remove(pia);
	}

	@Override
	public ProjectServiceEntity addServiceToProject(ProjectEntity project, ServiceEntity service, ProjectServiceType type, ProjectServiceStatusType status) {
		ProjectServiceEntity entity = new ProjectServiceEntity();
		entity.setProject(project);
		entity.setService(service);
		entity.setType(type);
		entity.setStatus(status);
		em.persist(entity);
		return entity;
	}

	@Override
	public ProjectMembershipEntity addMemberToProject(ProjectEntity project, IdentityEntity identity, ProjectMembershipType type) {
		ProjectMembershipEntity entity = new ProjectMembershipEntity();
		entity.setProject(project);
		entity.setIdentity(identity);
		entity.setMembershipType(type);
		em.persist(entity);
		return entity;
	}

	@Override
	public void deleteMembership(ProjectMembershipEntity entity) {
		if (! em.contains(entity))
			entity = em.merge(entity);
		em.remove(entity);
	}
	
	@Override
	public void deleteProjectService(ProjectServiceEntity entity) {
		if (! em.contains(entity))
			entity = em.merge(entity);
		em.remove(entity);
	}
}
