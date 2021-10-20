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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.oidc.ServiceOidcClientDao;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.ServiceOidcClientEntity;
import edu.kit.scc.webreg.entity.oidc.ServiceOidcClientEntity_;

@Named
@ApplicationScoped
public class JpaServiceOidcClientDao extends JpaBaseDao<ServiceOidcClientEntity> implements ServiceOidcClientDao {

	@Override
	public List<ServiceOidcClientEntity> findByClientConfig(OidcClientConfigurationEntity clientConfig) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceOidcClientEntity> criteria = builder.createQuery(ServiceOidcClientEntity.class);
		Root<ServiceOidcClientEntity> root = criteria.from(ServiceOidcClientEntity.class);
		criteria.where(builder.equal(root.get(ServiceOidcClientEntity_.clientConfig), clientConfig));
		criteria.orderBy(builder.asc(root.get(ServiceOidcClientEntity_.orderCriteria)));
		criteria.select(root);
		return em.createQuery(criteria).getResultList();
	}

	@Override
	public Class<ServiceOidcClientEntity> getEntityClass() {
		return ServiceOidcClientEntity.class;
	}
}
