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
package edu.kit.scc.webreg.dao.jpa.oidc;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcFlowStateDao;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity_;

@Named
@ApplicationScoped
public class JpaOidcFlowStateDao extends JpaBaseDao<OidcFlowStateEntity, Long> implements OidcFlowStateDao {

	@Override
	public OidcFlowStateEntity findByCode(String code) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OidcFlowStateEntity> criteria = builder.createQuery(OidcFlowStateEntity.class);
		Root<OidcFlowStateEntity> root = criteria.from(OidcFlowStateEntity.class);
		criteria.where(builder.equal(root.get(OidcFlowStateEntity_.code), code));
		criteria.select(root);
		return em.createQuery(criteria).getSingleResult();
	}	

	@Override
	public OidcFlowStateEntity findByAccessToken(String accessToken, String accessTokenType) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OidcFlowStateEntity> criteria = builder.createQuery(OidcFlowStateEntity.class);
		Root<OidcFlowStateEntity> root = criteria.from(OidcFlowStateEntity.class);
		criteria.where(builder.and(
				builder.equal(root.get(OidcFlowStateEntity_.accessToken), accessToken),
				builder.equal(root.get(OidcFlowStateEntity_.accessTokenType), accessTokenType)
				));
		criteria.select(root);
		return em.createQuery(criteria).getSingleResult();
	}	

	@Override
	public Class<OidcFlowStateEntity> getEntityClass() {
		return OidcFlowStateEntity.class;
	}
}
