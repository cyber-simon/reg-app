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
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcClientConfigurationDao;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity_;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;

@Named
@ApplicationScoped
public class JpaOidcClientConfigurationDao extends JpaBaseDao<OidcClientConfigurationEntity, Long> implements OidcClientConfigurationDao {

	@Override
	public OidcClientConfigurationEntity findByNameAndOp(String name, OidcOpConfigurationEntity opConfiguration) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OidcClientConfigurationEntity> criteria = builder.createQuery(OidcClientConfigurationEntity.class);
		Root<OidcClientConfigurationEntity> root = criteria.from(OidcClientConfigurationEntity.class);
		criteria.where(
			builder.and(
					builder.equal(root.get(OidcClientConfigurationEntity_.name), name),
					builder.equal(root.get(OidcClientConfigurationEntity_.opConfiguration), opConfiguration)
			)
		);
		criteria.select(root);
		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Class<OidcClientConfigurationEntity> getEntityClass() {
		return OidcClientConfigurationEntity.class;
	}
}
