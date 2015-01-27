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

import edu.kit.scc.webreg.dao.ApproverRoleDao;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaApproverRoleDao extends JpaBaseDao<ApproverRoleEntity, Long> implements ApproverRoleDao {

    @Override
	public List<ApproverRoleEntity> findWithServices(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ApproverRoleEntity> criteria = builder.createQuery(ApproverRoleEntity.class);
		Root<ApproverRoleEntity> root = criteria.from(ApproverRoleEntity.class);
		Root<UserEntity> userRoot = criteria.from(UserEntity.class);
		
		CriteriaQuery<ApproverRoleEntity> select = criteria.select(root);
		select.where(builder.equal(userRoot.get("id"), user.getId())).distinct(true);
		root.fetch("approverForServices", JoinType.LEFT);
		return em.createQuery(select).getResultList();
	}

	@Override
	public ApproverRoleEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ApproverRoleEntity> criteria = builder.createQuery(ApproverRoleEntity.class);
		Root<ApproverRoleEntity> root = criteria.from(ApproverRoleEntity.class);
		criteria.where(builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public ApproverRoleEntity findByName(String name) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ApproverRoleEntity> criteria = builder.createQuery(ApproverRoleEntity.class);
		Root<ApproverRoleEntity> role = criteria.from(ApproverRoleEntity.class);
		criteria.where(
				builder.equal(role.get("name"), name));
		criteria.select(role);
		
		return em.createQuery(criteria).getSingleResult();
	}	
	
	@Override
	public Class<ApproverRoleEntity> getEntityClass() {
		return ApproverRoleEntity.class;
	}
}
