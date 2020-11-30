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
import edu.kit.scc.webreg.dao.SamlIdpConfigurationDao;
import edu.kit.scc.webreg.dao.ServiceSamlSpDao;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity;
import edu.kit.scc.webreg.service.SamlIdpConfigurationService;

@Stateless
public class SamlIdpConfigurationServiceImpl extends BaseServiceImpl<SamlIdpConfigurationEntity, Long> implements SamlIdpConfigurationService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlIdpConfigurationDao dao;
	
	@Inject
	private ServiceSamlSpDao serviceSamlSpDao;

	@Override
	public SamlIdpConfigurationEntity findByEntityId(String entityId) {
		return dao.findByEntityId(entityId);
	}

	@Override
	public List<SamlIdpConfigurationEntity> findByHostname(String hostname) {
		return dao.findByHostname(hostname);
	}

	@Override
	public List<ServiceSamlSpEntity> findBySamlSpAndIdp(SamlIdpConfigurationEntity idpConfig, 
			SamlSpMetadataEntity spMetadata) {
		return serviceSamlSpDao.findBySamlSpAndIdp(idpConfig, spMetadata);
	}
	
	@Override
	protected BaseDao<SamlIdpConfigurationEntity, Long> getDao() {
		return dao;
	}

}
