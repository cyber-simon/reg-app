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
import edu.kit.scc.webreg.dao.SamlIdpConfigurationDao;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlIdpConfigurationService;

@Stateless
public class SamlIdpConfigurationServiceImpl extends BaseServiceImpl<SamlIdpConfigurationEntity, Long> implements SamlIdpConfigurationService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlIdpConfigurationDao dao;
	
	@Override
	public SamlIdpConfigurationEntity findByEntityId(String entityId) {
		return dao.findByEntityId(entityId);
	}

	@Override
	public SamlIdpConfigurationEntity findByHostname(String hostname) {
		return dao.findByHostname(hostname);
	}

	@Override
	protected BaseDao<SamlIdpConfigurationEntity, Long> getDao() {
		return dao;
	}

}
