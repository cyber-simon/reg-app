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
import edu.kit.scc.webreg.dao.SamlAuthnRequestDao;
import edu.kit.scc.webreg.entity.SamlAuthnRequestEntity;
import edu.kit.scc.webreg.service.SamlAuthnRequestService;

@Stateless
public class SamlAuthnRequestServiceImpl extends BaseServiceImpl<SamlAuthnRequestEntity, Long> implements SamlAuthnRequestService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlAuthnRequestDao dao;
	
	@Override
	public void deleteInvalid() {
		dao.deleteInvalid();
	}
	
	@Override
	protected BaseDao<SamlAuthnRequestEntity, Long> getDao() {
		return dao;
	}

}
