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

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.entity.audit.AuditDetailEntity;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class AuditDetailServiceImpl extends BaseServiceImpl<AuditDetailEntity, Long> implements AuditDetailService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AuditDetailDao dao;
	
	@Override
	public List<AuditDetailEntity> findNewestFailed(int limit) {
		return dao.findNewestFailed(limit);
	}

	@Override
	public List<AuditDetailEntity> findAllByAuditEntry(AuditEntryEntity auditEntry) {
		return dao.findAllByAuditEntry(auditEntry);
	}

	@Override
	protected BaseDao<AuditDetailEntity, Long> getDao() {
		return dao;
	}
}
