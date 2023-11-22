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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.SamlAAConfigurationDao;
import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;

@Named
@ApplicationScoped
public class JpaSamlAAConfigurationDao extends JpaBaseDao<SamlAAConfigurationEntity> implements SamlAAConfigurationDao {

	@Override
	public SamlAAConfigurationEntity findByHostname(String hostname) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlAAConfigurationEntity> criteria = builder.createQuery(SamlAAConfigurationEntity.class);
		Root<SamlAAConfigurationEntity> root = criteria.from(SamlAAConfigurationEntity.class);
		ListJoin<SamlAAConfigurationEntity, String> elementJoin = root.joinList("hostNameList");

		criteria.select(root);
		criteria.where(builder.equal(elementJoin.as(String.class), hostname));

		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Class<SamlAAConfigurationEntity> getEntityClass() {
		return SamlAAConfigurationEntity.class;
	}

}
