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

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.service.ASUserAttrService;

@Stateless
public class ASUserAttrServiceImpl extends BaseServiceImpl<ASUserAttrEntity, Long> implements ASUserAttrService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ASUserAttrDao dao;

	@Override
	public List<ASUserAttrEntity> findForUser(UserEntity user) {
		return dao.findForUser(user);
	}
	
	@Override
	protected BaseDao<ASUserAttrEntity, Long> getDao() {
		return dao;
	}
}
