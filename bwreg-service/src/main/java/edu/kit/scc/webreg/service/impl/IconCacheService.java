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

import java.io.Serializable;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.jpa.IconCacheDao;
import edu.kit.scc.webreg.entity.IconCacheEntity;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class IconCacheService extends BaseServiceImpl<IconCacheEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private IconCacheDao dao;

	@Override
	protected BaseDao<IconCacheEntity> getDao() {
		return dao;
	}

}
