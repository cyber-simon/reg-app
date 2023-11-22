/*
 * *****************************************************************************
 * Copyright (c) 2014 Michael Simon.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Michael Simon - initial
 * *****************************************************************************
 */
package edu.kit.scc.webreg.service.project;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.project.AttributeSourceProjectDao;
import edu.kit.scc.webreg.entity.project.AttributeSourceProjectEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class AttributeSourceProjectServiceImpl extends BaseServiceImpl<AttributeSourceProjectEntity>
		implements AttributeSourceProjectService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AttributeSourceProjectDao dao;

	@Override
	protected BaseDao<AttributeSourceProjectEntity> getDao() {
		return dao;
	}

}
