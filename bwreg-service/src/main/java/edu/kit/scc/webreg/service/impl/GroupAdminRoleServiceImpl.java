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

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GroupAdminRoleDao;
import edu.kit.scc.webreg.entity.GroupAdminRoleEntity;
import edu.kit.scc.webreg.service.GroupAdminRoleService;

@Stateless
public class GroupAdminRoleServiceImpl extends BaseServiceImpl<GroupAdminRoleEntity> implements GroupAdminRoleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private GroupAdminRoleDao dao;

	@Override
	protected BaseDao<GroupAdminRoleEntity> getDao() {
		return dao;
	}

}
