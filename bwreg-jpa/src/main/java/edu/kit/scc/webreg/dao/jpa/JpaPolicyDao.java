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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.PolicyDao;
import edu.kit.scc.webreg.entity.PolicyEntity;

@Named
@ApplicationScoped
public class JpaPolicyDao extends JpaBaseDao<PolicyEntity, Long> implements PolicyDao {

    @Override
	public PolicyEntity findWithAgreemets(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<PolicyEntity> criteria = builder.createQuery(PolicyEntity.class);
		Root<PolicyEntity> root = criteria.from(PolicyEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("agreementTexts", JoinType.LEFT);

		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public Class<PolicyEntity> getEntityClass() {
		return PolicyEntity.class;
	}
}
