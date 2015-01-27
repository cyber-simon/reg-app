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
import edu.kit.scc.webreg.dao.BusinessRulePackageDao;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.service.BusinessRulePackageService;

@Stateless
public class BusinessRulePackageServiceImpl extends BaseServiceImpl<BusinessRulePackageEntity, Long> implements BusinessRulePackageService {

	private static final long serialVersionUID = 1L;

	@Inject
	private BusinessRulePackageDao dao;
	
	@Override
	public List<BusinessRulePackageEntity> findAllNewer(Date date) {
		return dao.findAllNewer(date);
	}

	@Override
	public List<BusinessRulePackageEntity> findAllWithRules() {
		return dao.findAllWithRules();
	}

	@Override
	public BusinessRulePackageEntity findByNameAndVersion(String baseName, String version) {
		return dao.findByNameAndVersion(baseName, version);
	}

	@Override
	protected BaseDao<BusinessRulePackageEntity, Long> getDao() {
		return dao;
	}
}
