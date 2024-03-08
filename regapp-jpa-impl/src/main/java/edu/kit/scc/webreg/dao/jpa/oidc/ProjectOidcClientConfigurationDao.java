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

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity_;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.ProjectOidcClientConfigurationEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class ProjectOidcClientConfigurationDao extends JpaBaseDao<ProjectOidcClientConfigurationEntity> {

	public ProjectOidcClientConfigurationEntity findByNameAndOp(String name, OidcOpConfigurationEntity opConfiguration) {
		return find(and(equal(OidcClientConfigurationEntity_.name, name),
				equal(OidcClientConfigurationEntity_.opConfiguration, opConfiguration)));
	}

	@Override
	public Class<ProjectOidcClientConfigurationEntity> getEntityClass() {
		return ProjectOidcClientConfigurationEntity.class;
	}

}
