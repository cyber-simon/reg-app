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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;
import edu.kit.scc.webreg.entity.project.ExternalProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipType;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceType;

@Named
@ApplicationScoped
public class JpaProjectDao extends JpaBaseDao<ProjectEntity, Long> implements ProjectDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectEntity> findByService(ServiceEntity service) {
		return em.createQuery("select r.project from ProjectServiceEntity r where r.service = :service order by r.project.name")
			.setParameter("service", service).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectIdentityAdminEntity> findAdminByIdentity(IdentityEntity identity) {
		return em.createQuery("select r from ProjectIdentityAdminEntity r where r.identity = :identity order by r.project.name")
				.setParameter("identity", identity).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectMembershipEntity> findMembersForProject(ProjectEntity project) {
		return em.createQuery("select r from ProjectMembershipEntity r where r.project = :project")
				.setParameter("project", project).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectIdentityAdminEntity> findAdminsForProject(ProjectEntity project) {
		return em.createQuery("select r from ProjectIdentityAdminEntity r where r.project = :project")
				.setParameter("project", project).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectServiceEntity> findServicesForProject(ProjectEntity project) {
		return em.createQuery("select r from ProjectServiceEntity r where r.project = :project")
				.setParameter("project", project).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExternalProjectEntity> findByExternalName(String externalName) {
		return em.createQuery("select r from ExternalProjectEntity r where r.externalName = :externalName order by r.name")
			.setParameter("externalName", externalName).getResultList();
	}

	@Override
	public ExternalOidcProjectEntity findByExternalNameOidc(String externalName, OidcRpConfigurationEntity rpConfig) {
		try {
			return (ExternalOidcProjectEntity) em.createQuery("select r from ExternalOidcProjectEntity r "
					+ "where r.externalName = :externalName and r.rpConfig = :rpConfig order by r.name")
					.setParameter("externalName", externalName)
					.setParameter("rpConfig", rpConfig)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
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
	public ProjectServiceEntity addServiceToProject(ProjectEntity project, ServiceEntity service, ProjectServiceType type) {
		ProjectServiceEntity entity = new ProjectServiceEntity();
		entity.setProject(project);
		entity.setService(service);
		entity.setType(type);
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
		
	@Override
	public Class<ProjectEntity> getEntityClass() {
		return ProjectEntity.class;
	}
}
