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
import edu.kit.scc.webreg.dao.as.AttributeSourceServiceDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceServiceEntity;
import edu.kit.scc.webreg.service.AttributeSourceServiceService;

@Stateless
public class AttributeSourceServiceServiceImpl extends BaseServiceImpl<AttributeSourceServiceEntity>
		implements AttributeSourceServiceService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AttributeSourceServiceDao dao;

	@Override
	public AttributeSourceServiceEntity connectService(AttributeSourceEntity as, ServiceEntity service) {
		return dao.connectService(as, service);
	}
	
	@Override
	public void disconnectService(AttributeSourceEntity as, ServiceEntity service) {
		dao.disconnectService(as, service);
	}
	
	@Override
	protected BaseDao<AttributeSourceServiceEntity> getDao() {
		return dao;
	}
}
