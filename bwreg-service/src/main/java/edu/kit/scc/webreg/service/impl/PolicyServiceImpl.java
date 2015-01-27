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
import edu.kit.scc.webreg.dao.PolicyDao;
import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.service.PolicyService;

@Stateless
public class PolicyServiceImpl extends BaseServiceImpl<PolicyEntity, Long> implements PolicyService {

	private static final long serialVersionUID = 1;

	@Inject
	private PolicyDao dao;
	
	@Override
	public PolicyEntity findWithAgreemets(Long id) {
		return dao.findWithAgreemets(id);
	}

	@Override
	protected BaseDao<PolicyEntity, Long> getDao() {
		return dao;
	}
}
