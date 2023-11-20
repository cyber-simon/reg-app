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

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SamlAAMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlAAMetadataEntity;
import edu.kit.scc.webreg.service.SamlAAMetadataService;

@Stateless
public class SamlAAMetadataServiceImpl extends BaseServiceImpl<SamlAAMetadataEntity> implements SamlAAMetadataService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlAAMetadataDao dao;

	@Override
	public List<SamlAAMetadataEntity> findAllByFederation(FederationEntity federation) {
		return dao.findAllByFederation(federation);
	}

	@Override
	public SamlAAMetadataEntity findByEntityId(String entityId) {
		return dao.findByEntityId(entityId);
	}

	@Override
	protected BaseDao<SamlAAMetadataEntity> getDao() {
		return dao;
	}

}
