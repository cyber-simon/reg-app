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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.AdminRoleDao;
import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaAdminRoleDao extends JpaBaseDao<AdminRoleEntity, Long> implements AdminRoleDao {

    @Override
	public List<AdminRoleEntity> findWithServices(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<AdminRoleEntity> criteria = builder.createQuery(AdminRoleEntity.class);
		Root<AdminRoleEntity> root = criteria.from(AdminRoleEntity.class);
		Root<UserEntity> userRoot = criteria.from(UserEntity.class);
		
		CriteriaQuery<AdminRoleEntity> select = criteria.select(root);
		select.where(builder.equal(userRoot.get("id"), user.getId())).distinct(true);
		root.fetch("adminForServices", JoinType.LEFT);
		return em.createQuery(select).getResultList();
	}

	@Override
	public AdminRoleEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<AdminRoleEntity> criteria = builder.createQuery(AdminRoleEntity.class);
		Root<AdminRoleEntity> root = criteria.from(AdminRoleEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public AdminRoleEntity findByName(String name) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<AdminRoleEntity> criteria = builder.createQuery(AdminRoleEntity.class);
		Root<AdminRoleEntity> role = criteria.from(AdminRoleEntity.class);
		criteria.where(
				builder.equal(role.get("name"), name));
		criteria.select(role);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public Class<AdminRoleEntity> getEntityClass() {
		return AdminRoleEntity.class;
	}	
}
