/*
 * *****************************************************************************
 * Copyright (c) 2014 Michael Simon.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Michael Simon - initial
 * *****************************************************************************
 */
package edu.kit.scc.webreg.dao.jpa.project;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.project.ExternalOidcProjectDao;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity_;
import edu.kit.scc.webreg.entity.project.ExternalProjectEntity_;

@Named
@ApplicationScoped
public class JpaExternalOidcProjectDao extends JpaExternalProjectDao<ExternalOidcProjectEntity> implements ExternalOidcProjectDao {

	@Override
	public ExternalOidcProjectEntity findByExternalNameOidc(String externalName, OidcRpConfigurationEntity rpConfig) {
		return find(and(equal(ExternalProjectEntity_.externalName, externalName), equal(ExternalOidcProjectEntity_.rpConfig, rpConfig)));
	}

	@Override
	public Class<ExternalOidcProjectEntity> getEntityClass() {
		return ExternalOidcProjectEntity.class;
	}

}
