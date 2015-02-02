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

import edu.kit.scc.webreg.dao.SamlAAMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlAAMetadataEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;

@Named
@ApplicationScoped
public class JpaSamlAAMetadataDao extends JpaBaseDao<SamlAAMetadataEntity, Long> implements SamlAAMetadataDao {

	@Override
	public SamlAAMetadataEntity findByIdWithAll(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlAAMetadataEntity> criteria = builder.createQuery(SamlAAMetadataEntity.class);
		Root<SamlAAMetadataEntity> user = criteria.from(SamlAAMetadataEntity.class);
		criteria.where(builder.equal(user.get("id"), id));
		criteria.select(user);
		criteria.distinct(true);
		user.fetch("scopes", JoinType.LEFT);
		user.fetch("genericStore", JoinType.LEFT);
		user.fetch("federations", JoinType.LEFT);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}			
	}	
	
    @SuppressWarnings("unchecked")
	@Override
	public List<SamlAAMetadataEntity> findAllByFederation(FederationEntity federation) {
		return em.createQuery(
				"select distinct e from SamlAAMetadataEntity e join e.federations f where f = :fed")
				.setParameter("fed", federation).getResultList();
	}	

    @SuppressWarnings("unchecked")
	@Override
	public List<SamlAAMetadataEntity> findAllByStatusOrderedByOrgname(SamlMetadataEntityStatus status) {
		return em.createQuery(
				"select distinct e from SamlAAMetadataEntity e where e.status = :status order by e.orgName asc")
				.setParameter("status", status).getResultList();
	}	
    
	@Override
	public SamlAAMetadataEntity findByEntityId(String entityId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlAAMetadataEntity> criteria = builder.createQuery(SamlAAMetadataEntity.class);
		Root<SamlAAMetadataEntity> root = criteria.from(SamlAAMetadataEntity.class);
		criteria.where(
				builder.equal(root.get("entityId"), entityId));
		criteria.select(root);
		
		List<SamlAAMetadataEntity> idps = em.createQuery(criteria).getResultList();
		if (idps.size() < 1)
			return null;
		else
			return idps.get(0);
	}	

	@Override
	@SuppressWarnings("unchecked")
	public SamlAAMetadataEntity findByScope(String scope) {
		List<SamlAAMetadataEntity> idpList = em.createQuery(
				"select e from SamlAAMetadataEntity as e join e.scopes as s where s.scope = :scope")
				.setParameter("scope", scope).getResultList();
		
		/*
		 * Always return first idp found for scope. Could be more than one.
		 */
		
		if (idpList.size() == 0)
			return null;
		else
			return idpList.get(0);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SamlAAMetadataEntity> findAllByFederationOrderByOrgname(FederationEntity federation) {
		return em.createQuery(
				"select distinct e from SamlAAMetadataEntity e join e.federations f where f = :fed order by e.orgName asc")
				.setParameter("fed", federation).getResultList();
	}	
	
	@Override
	public Class<SamlAAMetadataEntity> getEntityClass() {
		return SamlAAMetadataEntity.class;
	}
}
