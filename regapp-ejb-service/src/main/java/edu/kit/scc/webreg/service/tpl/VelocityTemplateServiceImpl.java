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
package edu.kit.scc.webreg.service.tpl;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.VelocityTemplateDao;
import edu.kit.scc.webreg.entity.VelocityTemplateEntity;
import edu.kit.scc.webreg.entity.VelocityTemplateEntity_;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class VelocityTemplateServiceImpl extends BaseServiceImpl<VelocityTemplateEntity>
		implements VelocityTemplateService {

	private static final long serialVersionUID = 1L;

	@Inject
	private VelocityTemplateDao dao;

	@Override
	protected BaseDao<VelocityTemplateEntity> getDao() {
		return dao;
	}

	@Override
	public VelocityTemplateEntity findByName(String name) {
		return dao.find(equal(VelocityTemplateEntity_.name, name));
	}

}
