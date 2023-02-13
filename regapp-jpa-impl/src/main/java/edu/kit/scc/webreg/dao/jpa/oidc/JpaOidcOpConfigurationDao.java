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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcOpConfigurationDao;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity_;

@Named
@ApplicationScoped
public class JpaOidcOpConfigurationDao extends JpaBaseDao<OidcOpConfigurationEntity> implements OidcOpConfigurationDao {

	@Override
	public OidcOpConfigurationEntity findByRealmAndHost(String realm, String host) {
		return find(
				and(equal(OidcOpConfigurationEntity_.realm, realm), equal(OidcOpConfigurationEntity_.host, host)));
	}

	@Override
	public Class<OidcOpConfigurationEntity> getEntityClass() {
		return OidcOpConfigurationEntity.class;
	}

}
