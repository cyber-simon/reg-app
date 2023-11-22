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

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.oidc.ServiceOidcClientDao;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.ServiceOidcClientEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class ServiceOidcClientServiceImpl extends BaseServiceImpl<ServiceOidcClientEntity> implements ServiceOidcClientService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceOidcClientDao dao;
	
	@Override
	public List<ServiceOidcClientEntity> findByClientConfig(OidcClientConfigurationEntity clientConfig) {
		return dao.findByClientConfig(clientConfig);
	}
	
	@Override
	protected BaseDao<ServiceOidcClientEntity> getDao() {
		return dao;
	}
}
