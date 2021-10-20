/*******************************************************************************
 * Copyright (c) 2021 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.dao.jpa.oidc;

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

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcGroupDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcGroupEntity;

@Named
@ApplicationScoped
public class JpaOidcGroupDao extends JpaBaseDao<OidcGroupEntity> implements OidcGroupDao {

    @Override
	public List<OidcGroupEntity> findByUser(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<OidcGroupEntity> criteria = builder.createQuery(OidcGroupEntity.class);
	    Root<UserEntity> userRoot = criteria.from(UserEntity.class);
	    criteria.where(builder.equal(userRoot.get("id"), user.getId()));
	    Join<UserEntity, OidcGroupEntity> users = userRoot.join("groups");
	    CriteriaQuery<OidcGroupEntity> cq = criteria.select(users);
	    TypedQuery<OidcGroupEntity> query = em.createQuery(cq);
	    return query.getResultList();
	}

	@Override
	public OidcGroupEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OidcGroupEntity> criteria = builder.createQuery(OidcGroupEntity.class);
		Root<OidcGroupEntity> root = criteria.from(OidcGroupEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public OidcGroupEntity findByGidNumber(Integer gid) {
		try {
			return em.createQuery("select e from HomeOrgGroupEntity e where e.gidNumber = :gidNumber", OidcGroupEntity.class)
				.setParameter("gidNumber", gid).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public OidcGroupEntity findByName(String name) {
		try {
			return em.createQuery("select e from OidcGroupEntity e where e.name = :name", OidcGroupEntity.class)
				.setParameter("name", name).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public OidcGroupEntity findByNameAndPrefix(String name, String prefix) {
		try {
			return em.createQuery("select e from OidcGroupEntity e where e.name = :name and e.prefix = :prefix", OidcGroupEntity.class)
				.setParameter("name", name).setParameter("prefix", prefix).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<OidcGroupEntity> findByNameListAndPrefix(List<String> nameList, String prefix) {
		if (nameList == null || nameList.size() == 0) {
			return new ArrayList<OidcGroupEntity>();
		}
		else {
			return em.createQuery("select e from HomeOrgGroupEntity e where e.prefix = :prefix and e.name in :nameList", OidcGroupEntity.class)
				.setParameter("nameList", nameList).setParameter("prefix", prefix).getResultList();
		}
	}

	@Override
	public Class<OidcGroupEntity> getEntityClass() {
		return OidcGroupEntity.class;
	}
}
