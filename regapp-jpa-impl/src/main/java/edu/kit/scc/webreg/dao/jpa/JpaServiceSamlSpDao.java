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
package edu.kit.scc.webreg.dao.jpa;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.ServiceSamlSpDao;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity_;

@Named
@ApplicationScoped
public class JpaServiceSamlSpDao extends JpaBaseDao<ServiceSamlSpEntity> implements ServiceSamlSpDao {

	@Override
	public List<ServiceSamlSpEntity> findBySamlSpAndIdp(SamlIdpConfigurationEntity idp, SamlSpMetadataEntity sp) {
		return findAll(and(equal(ServiceSamlSpEntity_.idp, idp), equal(ServiceSamlSpEntity_.sp, sp)));
	}

	@Override
	public List<ServiceSamlSpEntity> findBySamlSp(SamlSpMetadataEntity sp) {
		return findAll(equal(ServiceSamlSpEntity_.sp, sp));
	}

	@Override
	public Class<ServiceSamlSpEntity> getEntityClass() {
		return ServiceSamlSpEntity.class;
	}

}
