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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.LocalGroupDao;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaLocalGroupDao extends JpaBaseDao<LocalGroupEntity, Long> implements LocalGroupDao {

    @Override
	public List<LocalGroupEntity> findByUser(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<LocalGroupEntity> criteria = builder.createQuery(LocalGroupEntity.class);
	    Root<UserEntity> userRoot = criteria.from(UserEntity.class);
	    criteria.where(builder.equal(userRoot.get("id"), user.getId()));
	    Join<UserEntity, LocalGroupEntity> users = userRoot.join("groups");
	    CriteriaQuery<LocalGroupEntity> cq = criteria.select(users);
	    TypedQuery<LocalGroupEntity> query = em.createQuery(cq);
	    return query.getResultList();
	}

	@Override
	public LocalGroupEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<LocalGroupEntity> criteria = builder.createQuery(LocalGroupEntity.class);
		Root<LocalGroupEntity> root = criteria.from(LocalGroupEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public LocalGroupEntity findWithUsersAndChildren(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<LocalGroupEntity> criteria = builder.createQuery(LocalGroupEntity.class);
		Root<LocalGroupEntity> root = criteria.from(LocalGroupEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		root.fetch("children", JoinType.LEFT);
		root.fetch("adminRoles", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public LocalGroupEntity findByGidNumber(Integer gid) {
		try {
			return (LocalGroupEntity) em.createQuery("select e from LocalGroupEntity e where e.gidNumber = :gidNumber")
				.setParameter("gidNumber", gid).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public LocalGroupEntity findByName(String name) {
		try {
			return (LocalGroupEntity) em.createQuery("select e from LocalGroupEntity e where e.name = :name")
				.setParameter("name", name).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public LocalGroupEntity findByNameAndPrefix(String name, String prefix) {
		try {
			return (LocalGroupEntity) em.createQuery("select e from LocalGroupEntity e where e.name = :name and e.prefix = :prefix")
				.setParameter("name", name).setParameter("prefix", prefix).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Class<LocalGroupEntity> getEntityClass() {
		return LocalGroupEntity.class;
	}
}
