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
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;

@Stateless
public class SamlIdpMetadataServiceImpl extends BaseServiceImpl<SamlIdpMetadataEntity>
		implements SamlIdpMetadataService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlIdpMetadataDao dao;

	@Override
	public List<SamlIdpMetadataEntity> findAllByFederation(FederationEntity federation) {
		return dao.findAllByFederation(federation);
	}

	@Override
	public SamlIdpMetadataEntity findByIdWithAll(Long id) {
		SamlIdpMetadataEntity idp = dao.findByIdWithAll(id);
		if (idp != null)
			idp.getEntityDescriptor();
		return idp;
	}

	@Override
	public SamlIdpMetadataEntity findByEntityId(String entityId) {
		return dao.findByEntityId(entityId);
	}

	@Override
	public SamlIdpMetadataEntity findByScope(String scope) {
		return dao.findByScope(scope);
	}

	@Override
	protected BaseDao<SamlIdpMetadataEntity> getDao() {
		return dao;
	}

}
