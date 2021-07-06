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
import edu.kit.scc.webreg.dao.ServiceSamlSpDao;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity;
import edu.kit.scc.webreg.service.ServiceSamlSpService;

@Stateless
public class ServiceSamlSpServiceImpl extends BaseServiceImpl<ServiceSamlSpEntity, Long> implements ServiceSamlSpService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceSamlSpDao dao;

	@Override
	protected BaseDao<ServiceSamlSpEntity, Long> getDao() {
		return dao;
	}

}
