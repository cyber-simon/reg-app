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
import edu.kit.scc.webreg.dao.TextPropertyDao;
import edu.kit.scc.webreg.entity.TextPropertyEntity;
import edu.kit.scc.webreg.service.TextPropertyService;

@Stateless
public class TextPropertyServiceImpl extends BaseServiceImpl<TextPropertyEntity, Long> implements TextPropertyService {

	private static final long serialVersionUID = 1L;

	@Inject
	private TextPropertyDao dao;
	
	@Override
	public TextPropertyEntity findAllBySingleton(String key, String language) {
		return dao.findByKeyAndLang(key, language);
	}

	@Override
	protected BaseDao<TextPropertyEntity, Long> getDao() {
		return dao;
	}
}
