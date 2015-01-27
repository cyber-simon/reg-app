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
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserRoleEntity;

@Named
@ApplicationScoped
public class JpaRoleDao extends JpaBaseDao<RoleEntity, Long> implements RoleDao {

	@Override
	public UserRoleEntity createNewUserRole() {
		return new UserRoleEntity();
	}
	
	@Override
	public void persistUserRole(UserRoleEntity userRole) {
		em.persist(userRole);
	}
	
	@Override
	public void deleteUserRole(Long userId, String roleName) {
		UserRoleEntity roleEntity = (UserRoleEntity) em.createQuery("select r from UserRoleEntity r where r.user.id = :userId "
				+ "and r.role.name = :roleName")
			.setParameter("userId", userId).setParameter("roleName", roleName).getSingleResult();
		em.remove(roleEntity);
	}
	
    @SuppressWarnings("unchecked")
	@Override
	public List<RoleEntity> findByUser(UserEntity user) {
		return em.createQuery("select r.role from UserRoleEntity r where r.user = :user")
				.setParameter("user", user).getResultList();
	}

    @SuppressWarnings("unchecked")
	@Override
	public List<RoleEntity> findByUserId(Long userId) {
		return em.createQuery("select r.role from UserRoleEntity r where r.user.id = :userId")
				.setParameter("userId", userId).getResultList();
	}

	@Override
	public RoleEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RoleEntity> criteria = builder.createQuery(RoleEntity.class);
		Root<RoleEntity> root = criteria.from(RoleEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public RoleEntity findByName(String name) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RoleEntity> criteria = builder.createQuery(RoleEntity.class);
		Root<RoleEntity> role = criteria.from(RoleEntity.class);
		criteria.where(
				builder.equal(role.get("name"), name));
		criteria.select(role);
		
		try {		
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public Class<RoleEntity> getEntityClass() {
		return RoleEntity.class;
	}
}
