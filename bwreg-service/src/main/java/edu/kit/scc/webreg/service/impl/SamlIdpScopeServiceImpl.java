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
import edu.kit.scc.webreg.dao.SamlIdpScopeDao;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;
import edu.kit.scc.webreg.service.SamlIdpScopeService;

@Stateless
public class SamlIdpScopeServiceImpl extends BaseServiceImpl<SamlIdpScopeEntity, Long> implements SamlIdpScopeService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlIdpScopeDao dao;
	
	@Override
	public List<SamlIdpScopeEntity> findByIdp(SamlIdpMetadataEntity idp) {
		return dao.findByIdp(idp);
	}

	@Override
	protected BaseDao<SamlIdpScopeEntity, Long> getDao() {
		return dao;
	}
}
