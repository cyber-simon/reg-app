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
package edu.kit.scc.webreg.audit;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.audit.AuditUserEntryDao;
import edu.kit.scc.webreg.entity.audit.AuditUserEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class AuditUserEntryServiceImpl extends BaseServiceImpl<AuditUserEntity, Long> implements AuditUserEntryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AuditUserEntryDao dao;
	
	@Override
	protected BaseDao<AuditUserEntity, Long> getDao() {
		return dao;
	}
}
