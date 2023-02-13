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
import edu.kit.scc.webreg.dao.oidc.OidcClientConfigurationDao;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity_;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;

@Named
@ApplicationScoped
public class JpaOidcClientConfigurationDao extends JpaBaseDao<OidcClientConfigurationEntity>
		implements OidcClientConfigurationDao {

	@Override
	public OidcClientConfigurationEntity findByNameAndOp(String name, OidcOpConfigurationEntity opConfiguration) {
		return find(and(equal(OidcClientConfigurationEntity_.name, name),
				equal(OidcClientConfigurationEntity_.opConfiguration, opConfiguration)));
	}

	@Override
	public Class<OidcClientConfigurationEntity> getEntityClass() {
		return OidcClientConfigurationEntity.class;
	}

}
