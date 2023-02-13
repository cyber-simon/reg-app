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
package edu.kit.scc.webreg.dao.jpa.project;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.project.ExternalOidcProjectDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity_;

@Named
@ApplicationScoped
public class JpaExternalOidcProjectDao extends JpaExternalProjectDao<ExternalOidcProjectEntity>
		implements ExternalOidcProjectDao {

	@Override
	public ExternalOidcProjectEntity findByExternalNameOidc(String externalName, OidcRpConfigurationEntity rpConfig) {
		return find(and(equal(ExternalOidcProjectEntity_.externalName, externalName),
				equal(ExternalOidcProjectEntity_.rpConfig, rpConfig)));
	}

	@Override
	public List<ExternalOidcProjectEntity> findByService(ServiceEntity service) {
		return findAll(unlimited(), ascendingBy("project.name"), equal("service", service));
	}

	@Override
	public Class<ExternalOidcProjectEntity> getEntityClass() {
		return ExternalOidcProjectEntity.class;
	}

}
