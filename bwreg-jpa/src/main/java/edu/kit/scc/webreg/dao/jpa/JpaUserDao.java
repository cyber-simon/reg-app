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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaUserDao extends JpaBaseDao<UserEntity, Long> implements UserDao, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
    @SuppressWarnings({"unchecked"})
	public List<UserEntity> findByPrimaryGroup(GroupEntity group) {
		return em.createQuery("select e from UserEntity e where e.primaryGroup = :primaryGroup")
				.setParameter("primaryGroup", group).getResultList();
	}

    @Override
    @SuppressWarnings({"unchecked"})
	public List<UserEntity> findOrderByUpdatedWithLimit(Date date, Integer limit) {
		return em.createQuery("select e from UserEntity e where lastUpdate < :date and lastFailedUpdate is null order by lastUpdate asc")
				.setParameter("date", date).setMaxResults(limit).getResultList();
	}

    @Override
    @SuppressWarnings({"unchecked"})
	public List<UserEntity> findOrderByFailedUpdateWithLimit(Date date, Integer limit) {
		return em.createQuery("select e from UserEntity e where lastFailedUpdate < :date order by lastFailedUpdate asc")
				.setParameter("date", date).setMaxResults(limit).getResultList();
	}

    @Override
    @SuppressWarnings({"unchecked"})
	public List<UserEntity> findGenericStoreKeyWithLimit(String key, Integer limit) {
		return em.createQuery("select e from UserEntity e join e.genericStore gs where key(gs) = :key order by lastUpdate asc")
				.setParameter("key", key).setMaxResults(limit).getResultList();
	}

    @Override
    @SuppressWarnings({"unchecked"})
	public List<UserEntity> findLegacyUsers() {
		return em.createQuery("select e from UserEntity e where e.idp is null").getResultList();
	}
 
    @Override
    @SuppressWarnings({"unchecked"})
	public List<UserEntity> findByGroup(GroupEntity group) {
		return em.createQuery("select e.user from UserGroupEntity e where e.group = :group")
				.setParameter("group", group).getResultList();
	}
    
	@Override
	public UserEntity findByPersistentWithRoles(String spId, String idpId, String persistentId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);
		Root<UserEntity> user = criteria.from(UserEntity.class);
		criteria.where(builder.and(
				builder.equal(user.get("persistentSpId"), spId),
				builder.equal(user.get("persistentIdpId"), idpId),
				builder.equal(user.get("persistentId"), persistentId)
				));
		criteria.select(user);
		criteria.distinct(true);
		user.fetch("roles", JoinType.LEFT);
		user.fetch("groups", JoinType.LEFT);
		user.fetch("genericStore", JoinType.LEFT);
		user.fetch("attributeStore", JoinType.LEFT);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}			
	}	

	@Override
	public UserEntity findByEppn(String eppn) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);
		Root<UserEntity> user = criteria.from(UserEntity.class);
		criteria.where(builder.equal(user.get("eppn"), eppn));
		criteria.select(user);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public UserEntity findByIdWithAll(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);
		Root<UserEntity> user = criteria.from(UserEntity.class);
		criteria.where(builder.and(
				builder.equal(user.get("id"), id)
				));
		criteria.select(user);
		criteria.distinct(true);
		user.fetch("roles", JoinType.LEFT);
		user.fetch("groups", JoinType.LEFT);
		user.fetch("genericStore", JoinType.LEFT);
		user.fetch("attributeStore", JoinType.LEFT);

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public UserEntity findByIdWithStore(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);
		Root<UserEntity> user = criteria.from(UserEntity.class);
		criteria.where(builder.and(
				builder.equal(user.get("id"), id)
				));
		criteria.select(user);
		criteria.distinct(true);
		user.fetch("genericStore", JoinType.LEFT);
		user.fetch("attributeStore", JoinType.LEFT);

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	
	
	@Override
	public Class<UserEntity> getEntityClass() {
		return UserEntity.class;
	}
}
