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

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.isNull;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.lessThan;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.util.Date;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity_;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class AuditEntryServiceImpl extends BaseServiceImpl<AuditEntryEntity> implements AuditEntryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private AuditEntryDao dao;

	@Override
	public void deleteAllOlderThan(Date date, int limit) {
		List<AuditEntryEntity> auditList = findAuditEntriesOlderThan(date, limit);

		logger.info("There are {} AuditEntries to be deleted", auditList.size());

		for (AuditEntryEntity audit : auditList) {
			logger.debug("Deleting audit {} with {} auditentries", audit.getId(), audit.getAuditDetails().size());
			dao.delete(audit);
		}
	}

	private List<AuditEntryEntity> findAuditEntriesOlderThan(Date date, int limit) {
		return dao.findAll(withLimit(limit), ascendingBy(AuditEntryEntity_.endTime),
				and(isNull(AuditEntryEntity_.parentEntry), lessThan(AuditEntryEntity_.endTime, date)));
	}

	@Override
	protected BaseDao<AuditEntryEntity> getDao() {
		return dao;
	}
}
