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
import edu.kit.scc.webreg.dao.SshPubKeyApproverRoleDao;
import edu.kit.scc.webreg.entity.SshPubKeyApproverRoleEntity;
import edu.kit.scc.webreg.service.SshPubKeyApproverRoleService;

@Stateless
public class SshPubKeyApproverRoleServiceImpl extends BaseServiceImpl<SshPubKeyApproverRoleEntity> implements SshPubKeyApproverRoleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SshPubKeyApproverRoleDao dao;
	
	@Override
	protected BaseDao<SshPubKeyApproverRoleEntity> getDao() {
		return dao;
	}
}
