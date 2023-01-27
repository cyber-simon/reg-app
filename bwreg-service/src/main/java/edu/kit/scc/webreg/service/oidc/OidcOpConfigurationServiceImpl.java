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
package edu.kit.scc.webreg.service.oidc;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.isNull;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcOpConfigurationDao;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationStatusType;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class OidcOpConfigurationServiceImpl extends BaseServiceImpl<OidcOpConfigurationEntity>
		implements OidcOpConfigurationService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private OidcOpConfigurationDao dao;

	@Override
	public OidcOpConfigurationEntity findByRealm(String realm) {
		return dao.findByRealm(realm);
	}

	@Override
	public OidcOpConfigurationEntity findByRealmAndHost(String realm, String host) {
		return dao.findByRealmAndHost(realm, host);
	}

	@Override
	public void fixStatus() {
		List<OidcOpConfigurationEntity> opList = dao.findAllPaging(isNull("opStatus"));
		for (OidcOpConfigurationEntity op : opList) {
			logger.info("Setting status from null to ACTIVE for OIDC OP configuration {}", op.getName());
			op.setOpStatus(OidcOpConfigurationStatusType.ACTIVE);
		}
	}

	@Override
	protected BaseDao<OidcOpConfigurationEntity> getDao() {
		return dao;
	}
}
