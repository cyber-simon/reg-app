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
import edu.kit.scc.webreg.dao.ClusterMemberDao;
import edu.kit.scc.webreg.entity.ClusterMemberEntity;
import edu.kit.scc.webreg.service.ClusterMemberService;

@Stateless
public class ClusterMemberServiceImpl extends BaseServiceImpl<ClusterMemberEntity, Long> implements ClusterMemberService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ClusterMemberDao dao;

	@Override
	protected BaseDao<ClusterMemberEntity, Long> getDao() {
		return dao;
	}
}
