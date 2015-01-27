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
import edu.kit.scc.webreg.dao.BusinessRuleDao;
import edu.kit.scc.webreg.entity.BusinessRuleEntity;
import edu.kit.scc.webreg.service.BusinessRuleService;

@Stateless
public class BusinessRuleServiceImpl extends BaseServiceImpl<BusinessRuleEntity, Long> implements BusinessRuleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private BusinessRuleDao dao;
	
	@Override
	public List<BusinessRuleEntity> findAllNewer(Date date) {
		return dao.findAllNewer(date);
	}

	@Override
	public List<BusinessRuleEntity> findAllKnowledgeBaseNotNull() {
		return dao.findAllKnowledgeBaseNotNull();
	}

	@Override
	protected BaseDao<BusinessRuleEntity, Long> getDao() {
		return dao;
	}
}
