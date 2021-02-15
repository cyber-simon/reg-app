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
import edu.kit.scc.webreg.dao.oidc.OidcOpConfigurationDao;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity_;

@Named
@ApplicationScoped
public class JpaOidcOpConfigurationDao extends JpaBaseDao<OidcOpConfigurationEntity, Long> implements OidcOpConfigurationDao {

	@Override
	public OidcOpConfigurationEntity findByRealm(String realm) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OidcOpConfigurationEntity> criteria = builder.createQuery(OidcOpConfigurationEntity.class);
		Root<OidcOpConfigurationEntity> root = criteria.from(OidcOpConfigurationEntity.class);
		criteria.where(builder.equal(root.get(OidcOpConfigurationEntity_.realm), realm));
		criteria.select(root);
		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}	
	
	@Override
	public OidcOpConfigurationEntity findByRealmAndHost(String realm, String host) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OidcOpConfigurationEntity> criteria = builder.createQuery(OidcOpConfigurationEntity.class);
		Root<OidcOpConfigurationEntity> root = criteria.from(OidcOpConfigurationEntity.class);
		criteria.where(builder.and(
				builder.equal(root.get(OidcOpConfigurationEntity_.realm), realm),
				builder.equal(root.get(OidcOpConfigurationEntity_.host), host)
				));
				
		criteria.select(root);
		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}	
	
	@Override
	public Class<OidcOpConfigurationEntity> getEntityClass() {
		return OidcOpConfigurationEntity.class;
	}
}
