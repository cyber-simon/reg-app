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
import edu.kit.scc.webreg.dao.oidc.OidcFlowStateDao;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class OidcFlowStateServiceImpl extends BaseServiceImpl<OidcFlowStateEntity, Long> implements OidcFlowStateService {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcFlowStateDao dao;
	
	@Override
	public void deleteExpiredTokens() {
		dao.deleteExpiredTokens();
	}
	
	@Override
	protected BaseDao<OidcFlowStateEntity, Long> getDao() {
		return dao;
	}
}
