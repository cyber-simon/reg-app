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
import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;

@Stateless
public class SamlSpConfigurationServiceImpl extends BaseServiceImpl<SamlSpConfigurationEntity, Long> implements SamlSpConfigurationService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlSpConfigurationDao dao;
	
	@Override
	public SamlSpConfigurationEntity findByEntityId(String entityId) {
		return dao.findByEntityId(entityId);
	}

	@Override
	public SamlSpConfigurationEntity findByHostname(String hostname) {
		return dao.findByHostname(hostname);
	}

	@Override
	protected BaseDao<SamlSpConfigurationEntity, Long> getDao() {
		return dao;
	}

}
