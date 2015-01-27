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

import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.entity.FederationEntity;

@Named
@ApplicationScoped
public class JpaFederationDao extends JpaBaseDao<FederationEntity, Long> implements FederationDao {

    @Override
	public List<FederationEntity> findAllWithIdpEntities() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<FederationEntity> criteria = builder.createQuery(FederationEntity.class);
		Root<FederationEntity> root = criteria.from(FederationEntity.class);
		criteria.select(root).distinct(true);
		root.fetch("idps", JoinType.LEFT);

		return em.createQuery(criteria).getResultList();
	}

	@Override
	public FederationEntity findWithIdpEntities(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<FederationEntity> criteria = builder.createQuery(FederationEntity.class);
		Root<FederationEntity> root = criteria.from(FederationEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("idps", JoinType.LEFT);

		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public Class<FederationEntity> getEntityClass() {
		return FederationEntity.class;
	}
}
