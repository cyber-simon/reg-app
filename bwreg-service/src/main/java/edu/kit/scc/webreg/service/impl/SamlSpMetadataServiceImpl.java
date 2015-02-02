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
import edu.kit.scc.webreg.dao.SamlSpMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.service.SamlSpMetadataService;

@Stateless
public class SamlSpMetadataServiceImpl extends BaseServiceImpl<SamlSpMetadataEntity, Long> implements SamlSpMetadataService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlSpMetadataDao dao;
	
	@Override
	public List<SamlSpMetadataEntity> findAllByFederation(FederationEntity federation) {
		return dao.findAllByFederation(federation);
	}
	
	@Override
	public List<SamlSpMetadataEntity> findAllByStatusOrderedByOrgname(SamlMetadataEntityStatus status) {
		return dao.findAllByStatusOrderedByOrgname(status);
	}
	
	@Override
	public SamlSpMetadataEntity findByIdWithAll(Long id) {
		SamlSpMetadataEntity sp = dao.findByIdWithAll(id);
		if (sp != null)
			sp.getEntityDescriptor();
		return sp;
	}
	
	@Override
	public SamlSpMetadataEntity findByEntityId(String entityId) {
		return dao.findByEntityId(entityId);
	}
	
	@Override
	public List<SamlSpMetadataEntity> findAllByFederationOrderByOrgname(FederationEntity federation) {
		return dao.findAllByFederationOrderByOrgname(federation);
	}
		
	@Override
	public SamlSpMetadataEntity findByScope(String scope) {
		return dao.findByScope(scope);
	}

	@Override
	protected BaseDao<SamlSpMetadataEntity, Long> getDao() {
		return dao;
	}

}
