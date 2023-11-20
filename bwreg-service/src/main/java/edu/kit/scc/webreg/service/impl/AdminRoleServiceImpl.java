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

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.AdminRoleDao;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.service.AdminRoleService;

@Stateless
public class AdminRoleServiceImpl extends BaseServiceImpl<AdminRoleEntity> implements AdminRoleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AdminRoleDao dao;

	@Override
	protected BaseDao<AdminRoleEntity> getDao() {
		return dao;
	}

}
