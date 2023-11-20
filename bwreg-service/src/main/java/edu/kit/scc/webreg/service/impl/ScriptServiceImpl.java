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

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ScriptDao;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.service.ScriptService;

@Stateless
public class ScriptServiceImpl extends BaseServiceImpl<ScriptEntity> implements ScriptService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ScriptDao dao;

	@Override
	protected BaseDao<ScriptEntity> getDao() {
		return dao;
	}
}
