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

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;
import static edu.kit.scc.webreg.dao.ops.SortBy.descendingBy;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.entity.audit.AuditDetailEntity;
import edu.kit.scc.webreg.entity.audit.AuditDetailEntity_;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class AuditDetailServiceImpl extends BaseServiceImpl<AuditDetailEntity> implements AuditDetailService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AuditDetailDao dao;

	@Override
	public List<AuditDetailEntity> findNewestFailed(int limit) {
		return dao.findAll(withLimit(limit), descendingBy(AuditDetailEntity_.endTime),
				equal(AuditDetailEntity_.auditStatus, AuditStatus.FAIL));
	}

	@Override
	public List<AuditDetailEntity> findAllByAuditEntry(AuditEntryEntity auditEntry) {
		return dao.findAll(unlimited(), ascendingBy(AuditDetailEntity_.endTime),
				equal(AuditDetailEntity_.auditEntry, auditEntry));
	}

	@Override
	protected BaseDao<AuditDetailEntity> getDao() {
		return dao;
	}
}
