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

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.AdminUserDao;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.AdminUserEntity_;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.service.AdminUserService;

@Stateless
public class AdminUserServiceImpl extends BaseServiceImpl<AdminUserEntity> implements AdminUserService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AdminUserDao dao;

	@Override
	public AdminUserEntity findByUsername(String username) {
		return dao.find(equal(AdminUserEntity_.username, username));
	}

	@Override
	public List<RoleEntity> findRolesForUserById(Long id) {
		return dao.findRolesForUserById(id);
	}

	@Override
	protected BaseDao<AdminUserEntity> getDao() {
		return dao;
	}
}
