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

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.Query;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcFlowStateDao;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;

@Named
@ApplicationScoped
public class JpaOidcFlowStateDao extends JpaBaseDao<OidcFlowStateEntity> implements OidcFlowStateDao {

	@Override
	public void deleteExpiredTokens() {
		Query query = em.createQuery("delete from OidcFlowStateEntity where validUntil <= :validUntil");
		query.setParameter("validUntil", new Date());
		query.executeUpdate();
	}

	@Override
	public Class<OidcFlowStateEntity> getEntityClass() {
		return OidcFlowStateEntity.class;
	}

}
