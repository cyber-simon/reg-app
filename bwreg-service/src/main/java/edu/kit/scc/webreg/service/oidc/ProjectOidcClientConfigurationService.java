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
package edu.kit.scc.webreg.service.oidc;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.jpa.oidc.ProjectOidcClientConfigurationDao;
import edu.kit.scc.webreg.entity.oidc.ProjectOidcClientConfigurationEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class ProjectOidcClientConfigurationService extends BaseServiceImpl<ProjectOidcClientConfigurationEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private ProjectOidcClientConfigurationDao dao;

	@Override
	protected BaseDao<ProjectOidcClientConfigurationEntity> getDao() {
		return dao;
	}
}
