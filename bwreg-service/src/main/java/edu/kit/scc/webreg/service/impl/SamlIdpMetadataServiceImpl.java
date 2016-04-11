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

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;

@Stateless
public class SamlIdpMetadataServiceImpl extends BaseServiceImpl<SamlIdpMetadataEntity, Long> implements SamlIdpMetadataService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlIdpMetadataDao dao;
	
	@Override
	public void updateIdpStatus(SamlIdpMetadataEntityStatus status, SamlIdpMetadataEntity idpEntity) {
		idpEntity = dao.merge(idpEntity);
		if (! status.equals(idpEntity.getIdIdpStatus())) {
			idpEntity.setIdIdpStatus(status);
			idpEntity.setLastIdStatusChange(new Date());
		}
	}
	
	@Override
	public List<SamlIdpMetadataEntity> findAllByFederation(FederationEntity federation) {
		return dao.findAllByFederation(federation);
	}
	
	@Override
	public List<SamlIdpMetadataEntity> findAllByStatusOrderedByOrgname(SamlMetadataEntityStatus status) {
		return dao.findAllByStatusOrderedByOrgname(status);
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
	public List<SamlIdpMetadataEntity> findAllByFederationOrderByOrgname(FederationEntity federation) {
		return dao.findAllByFederationOrderByOrgname(federation);
	}
		
	@Override
	public SamlIdpMetadataEntity findByScope(String scope) {
		return dao.findByScope(scope);
	}

	@Override
	protected BaseDao<SamlIdpMetadataEntity, Long> getDao() {
		return dao;
	}

}
