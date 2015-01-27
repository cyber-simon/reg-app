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

import edu.kit.scc.webreg.dao.AgreementTextDao;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.entity.AgreementTextEntity;
import edu.kit.scc.webreg.service.AgreementTextService;

@Stateless
public class AgreementTextServiceImpl extends BaseServiceImpl<AgreementTextEntity, Long> implements AgreementTextService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AgreementTextDao dao;
	
	@Override
	protected BaseDao<AgreementTextEntity, Long> getDao() {
		return dao;
	}
}
