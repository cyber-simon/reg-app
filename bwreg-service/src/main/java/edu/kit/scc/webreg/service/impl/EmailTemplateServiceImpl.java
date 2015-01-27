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
import edu.kit.scc.webreg.dao.EmailTemplateDao;
import edu.kit.scc.webreg.entity.EmailTemplateEntity;
import edu.kit.scc.webreg.service.EmailTemplateService;

@Stateless
public class EmailTemplateServiceImpl extends BaseServiceImpl<EmailTemplateEntity, Long> implements EmailTemplateService {

	private static final long serialVersionUID = 1L;

	@Inject
	private EmailTemplateDao dao;
	
	@Override
	protected BaseDao<EmailTemplateEntity, Long> getDao() {
		return dao;
	}
	
	@Override
	public EmailTemplateEntity findByName(String name) {
		return dao.findByName(name);
	}
}
