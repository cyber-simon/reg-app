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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.SamlAAConfigurationDao;
import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;

@Named
@ApplicationScoped
public class JpaSamlAAConfigurationDao extends JpaBaseDao<SamlAAConfigurationEntity, Long> implements SamlAAConfigurationDao {

    @Override
	public SamlAAConfigurationEntity findByEntityId(String entityId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlAAConfigurationEntity> criteria = builder.createQuery(SamlAAConfigurationEntity.class);
		Root<SamlAAConfigurationEntity> root = criteria.from(SamlAAConfigurationEntity.class);
		criteria.where(
				builder.equal(root.get("entityId"), entityId));
		criteria.select(root);

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	
	
	@Override
	public SamlAAConfigurationEntity findByHostname(String hostname) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlAAConfigurationEntity> criteria = builder.createQuery(SamlAAConfigurationEntity.class);
		Root<SamlAAConfigurationEntity> root = criteria.from(SamlAAConfigurationEntity.class);
		ListJoin<SamlAAConfigurationEntity, String> elementJoin = root.joinList("hostNameList");
		
		criteria.select(root);
		criteria.where(
				builder.equal(elementJoin.as(String.class), hostname));

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}		
	}

	@Override
	public Class<SamlAAConfigurationEntity> getEntityClass() {
		return SamlAAConfigurationEntity.class;
	}
}
