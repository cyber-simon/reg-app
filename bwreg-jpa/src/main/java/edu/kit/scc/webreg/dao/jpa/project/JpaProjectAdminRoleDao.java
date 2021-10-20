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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.project.ProjectAdminRoleDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminRoleEntity;

@Named
@ApplicationScoped
public class JpaProjectAdminRoleDao extends JpaBaseDao<ProjectAdminRoleEntity> implements ProjectAdminRoleDao {

    @Override
	public List<ProjectAdminRoleEntity> findWithServices(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ProjectAdminRoleEntity> criteria = builder.createQuery(ProjectAdminRoleEntity.class);
		Root<ProjectAdminRoleEntity> root = criteria.from(ProjectAdminRoleEntity.class);
		Root<UserEntity> userRoot = criteria.from(UserEntity.class);
		
		CriteriaQuery<ProjectAdminRoleEntity> select = criteria.select(root);
		select.where(builder.equal(userRoot.get("id"), user.getId())).distinct(true);
		root.fetch("adminForServices", JoinType.LEFT);
		return em.createQuery(select).getResultList();
	}

	@Override
	public ProjectAdminRoleEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ProjectAdminRoleEntity> criteria = builder.createQuery(ProjectAdminRoleEntity.class);
		Root<ProjectAdminRoleEntity> root = criteria.from(ProjectAdminRoleEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public ProjectAdminRoleEntity findByName(String name) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ProjectAdminRoleEntity> criteria = builder.createQuery(ProjectAdminRoleEntity.class);
		Root<ProjectAdminRoleEntity> role = criteria.from(ProjectAdminRoleEntity.class);
		criteria.where(
				builder.equal(role.get("name"), name));
		criteria.select(role);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public Class<ProjectAdminRoleEntity> getEntityClass() {
		return ProjectAdminRoleEntity.class;
	}	
}
