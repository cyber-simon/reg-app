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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.SamlIdpConfigurationDao;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;

@Named
@ApplicationScoped
public class JpaSamlIdpConfigurationDao extends JpaBaseDao<SamlIdpConfigurationEntity>
		implements SamlIdpConfigurationDao {

	@Override
	public List<SamlIdpConfigurationEntity> findByHostname(String hostname) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlIdpConfigurationEntity> criteria = builder.createQuery(SamlIdpConfigurationEntity.class);
		Root<SamlIdpConfigurationEntity> root = criteria.from(SamlIdpConfigurationEntity.class);
		ListJoin<SamlIdpConfigurationEntity, String> elementJoin = root.joinList("hostNameList");

		criteria.select(root);
		criteria.where(builder.equal(elementJoin.as(String.class), hostname));

		try {
			return em.createQuery(criteria).getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Class<SamlIdpConfigurationEntity> getEntityClass() {
		return SamlIdpConfigurationEntity.class;
	}
}
