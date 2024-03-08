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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.oidc.OidcClientConsumerEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConsumerEntity_;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class OidcClientConsumerDao extends JpaBaseDao<OidcClientConsumerEntity> {

	public OidcClientConsumerEntity findByName(String name) {
		return find(equal(OidcClientConsumerEntity_.name, name));
	}

	@Override
	public Class<OidcClientConsumerEntity> getEntityClass() {
		return OidcClientConsumerEntity.class;
	}

}
