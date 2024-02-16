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
package edu.kit.scc.webreg.service.impl;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.FederationEntity_;
import edu.kit.scc.webreg.service.FederationService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class FederationServiceImpl extends BaseServiceImpl<FederationEntity> implements FederationService {

	private static final long serialVersionUID = 1L;

	@Inject
	private FederationDao dao;

	@Override
	public FederationEntity findWithIdpEntities(Long id) {
		return dao.find(equal(FederationEntity_.id, id), FederationEntity_.idps);
	}

	@Override
	protected BaseDao<FederationEntity> getDao() {
		return dao;
	}
}
