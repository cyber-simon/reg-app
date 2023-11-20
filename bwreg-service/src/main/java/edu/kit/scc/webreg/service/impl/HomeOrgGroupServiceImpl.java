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

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.HomeOrgGroupDao;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity_;
import edu.kit.scc.webreg.service.HomeOrgGroupService;

@Stateless
public class HomeOrgGroupServiceImpl extends BaseServiceImpl<HomeOrgGroupEntity> implements HomeOrgGroupService {

	private static final long serialVersionUID = 1L;

	@Inject
	private HomeOrgGroupDao dao;

	@Override
	public HomeOrgGroupEntity findWithUsers(Long id) {
		return dao.find(equal(HomeOrgGroupEntity_.id, id), HomeOrgGroupEntity_.users);
	}

	@Override
	protected BaseDao<HomeOrgGroupEntity> getDao() {
		return dao;
	}

}
