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

import edu.kit.scc.webreg.dao.ApproverRoleDao;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.service.ApproverRoleService;

@Stateless
public class ApproverRoleServiceImpl extends BaseServiceImpl<ApproverRoleEntity> implements ApproverRoleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ApproverRoleDao dao;

	@Override
	protected BaseDao<ApproverRoleEntity> getDao() {
		return dao;
	}

}
