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

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpConfigurationDao;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class OidcRpConfigurationServiceImpl extends BaseServiceImpl<OidcRpConfigurationEntity> implements OidcRpConfigurationService {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcRpConfigurationDao dao;
	
	@Override
	protected BaseDao<OidcRpConfigurationEntity> getDao() {
		return dao;
	}
}
