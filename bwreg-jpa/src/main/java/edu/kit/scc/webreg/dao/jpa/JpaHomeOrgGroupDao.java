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

import java.util.ArrayList;
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

import edu.kit.scc.webreg.dao.HomeOrgGroupDao;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaHomeOrgGroupDao extends JpaBaseDao<HomeOrgGroupEntity, Long> implements HomeOrgGroupDao {

    @Override
	public List<HomeOrgGroupEntity> findByUser(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<HomeOrgGroupEntity> criteria = builder.createQuery(HomeOrgGroupEntity.class);
	    Root<UserEntity> userRoot = criteria.from(UserEntity.class);
	    criteria.where(builder.equal(userRoot.get("id"), user.getId()));
	    Join<UserEntity, HomeOrgGroupEntity> users = userRoot.join("groups");
	    CriteriaQuery<HomeOrgGroupEntity> cq = criteria.select(users);
	    TypedQuery<HomeOrgGroupEntity> query = em.createQuery(cq);
	    return query.getResultList();
	}

	@Override
	public HomeOrgGroupEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<HomeOrgGroupEntity> criteria = builder.createQuery(HomeOrgGroupEntity.class);
		Root<HomeOrgGroupEntity> root = criteria.from(HomeOrgGroupEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public HomeOrgGroupEntity findByGidNumber(Integer gid) {
		try {
			return (HomeOrgGroupEntity) em.createQuery("select e from HomeOrgGroupEntity e where e.gidNumber = :gidNumber")
				.setParameter("gidNumber", gid).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public HomeOrgGroupEntity findByName(String name) {
		try {
			return (HomeOrgGroupEntity) em.createQuery("select e from HomeOrgGroupEntity e where e.name = :name")
				.setParameter("name", name).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public HomeOrgGroupEntity findByNameAndPrefix(String name, String prefix) {
		try {
			return (HomeOrgGroupEntity) em.createQuery("select e from HomeOrgGroupEntity e where e.name = :name and e.prefix = :prefix")
				.setParameter("name", name).setParameter("prefix", prefix).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HomeOrgGroupEntity> findByNameListAndPrefix(List<String> nameList, String prefix) {
		if (nameList == null || nameList.size() == 0) {
			return new ArrayList<HomeOrgGroupEntity>();
		}
		else {
			return em.createQuery("select e from HomeOrgGroupEntity e where e.prefix = :prefix and e.name in :nameList")
				.setParameter("nameList", nameList).setParameter("prefix", prefix).getResultList();
		}
	}

	@Override
	public Class<HomeOrgGroupEntity> getEntityClass() {
		return HomeOrgGroupEntity.class;
	}
}
