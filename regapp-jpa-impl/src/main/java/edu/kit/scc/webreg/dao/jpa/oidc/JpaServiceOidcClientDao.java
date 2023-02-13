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

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

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
		return findAll(unlimited(), ascendingBy(ServiceOidcClientEntity_.orderCriteria),
				equal(ServiceOidcClientEntity_.clientConfig, clientConfig));
	}

	@Override
	public Class<ServiceOidcClientEntity> getEntityClass() {
		return ServiceOidcClientEntity.class;
	}

}
