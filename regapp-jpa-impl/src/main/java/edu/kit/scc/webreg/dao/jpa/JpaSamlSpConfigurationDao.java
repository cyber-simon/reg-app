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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity_;

@Named
@ApplicationScoped
public class JpaSamlSpConfigurationDao extends JpaBaseDao<SamlSpConfigurationEntity> implements SamlSpConfigurationDao {

	@Override
	public SamlSpConfigurationEntity findByEntityId(String entityId) {
		return find(equal(SamlSpConfigurationEntity_.entityId, entityId));
	}

	@Override
	public List<SamlSpConfigurationEntity> findByHostname(String hostname) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlSpConfigurationEntity> criteria = builder.createQuery(SamlSpConfigurationEntity.class);
		Root<SamlSpConfigurationEntity> root = criteria.from(SamlSpConfigurationEntity.class);
		ListJoin<SamlSpConfigurationEntity, String> elementJoin = root.joinList("hostNameList");

		criteria.select(root);
		criteria.where(builder.equal(elementJoin.as(String.class), hostname));

		return em.createQuery(criteria).getResultList();
	}

	@Override
	public Class<SamlSpConfigurationEntity> getEntityClass() {
		return SamlSpConfigurationEntity.class;
	}

}
