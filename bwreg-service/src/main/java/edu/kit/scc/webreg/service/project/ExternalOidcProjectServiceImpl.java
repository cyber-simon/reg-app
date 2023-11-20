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
package edu.kit.scc.webreg.service.project;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.project.ExternalOidcProjectDao;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class ExternalOidcProjectServiceImpl extends BaseServiceImpl<ExternalOidcProjectEntity> implements ExternalOidcProjectService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ExternalOidcProjectDao dao;

	@Override
	protected BaseDao<ExternalOidcProjectEntity> getDao() {
		return dao;
	}
}
