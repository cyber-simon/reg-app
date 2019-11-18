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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.entity.SamlUserEntity;

@Named
@ApplicationScoped
public class JpaSamlUserDao extends JpaBaseDao<SamlUserEntity, Long> implements SamlUserDao, Serializable {

	private static final long serialVersionUID = 1L;
    
	@Override
	public SamlUserEntity findByPersistentWithRoles(String spId, String idpId, String persistentId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlUserEntity> criteria = builder.createQuery(SamlUserEntity.class);
		Root<SamlUserEntity> user = criteria.from(SamlUserEntity.class);
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
	public SamlUserEntity findByPersistent(String spId, String idpId, String persistentId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlUserEntity> criteria = builder.createQuery(SamlUserEntity.class);
		Root<SamlUserEntity> user = criteria.from(SamlUserEntity.class);
		criteria.where(builder.and(
				builder.equal(user.get("persistentSpId"), spId),
				builder.equal(user.get("persistentIdpId"), idpId),
				builder.equal(user.get("persistentId"), persistentId)
				));
		criteria.select(user);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}			
	}	
	
	@Override
	public SamlUserEntity findByEppn(String eppn) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlUserEntity> criteria = builder.createQuery(SamlUserEntity.class);
		Root<SamlUserEntity> user = criteria.from(SamlUserEntity.class);
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
	public SamlUserEntity findByIdWithStore(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlUserEntity> criteria = builder.createQuery(SamlUserEntity.class);
		Root<SamlUserEntity> user = criteria.from(SamlUserEntity.class);
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
	public Class<SamlUserEntity> getEntityClass() {
		return SamlUserEntity.class;
	}
}
