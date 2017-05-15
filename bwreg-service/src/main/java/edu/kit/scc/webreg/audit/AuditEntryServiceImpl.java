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

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class AuditEntryServiceImpl extends BaseServiceImpl<AuditEntryEntity, Long> implements AuditEntryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private AuditEntryDao dao;
	
	@Override
	public List<AuditEntryEntity> findAllOlderThan(Date date, int limit) {
		return dao.findAllOlderThan(date, limit);
	}

	@Override
	public void deleteAllOlderThan(Date date, int limit) {
		List<AuditEntryEntity> auditList = dao.findAllOlderThan(date, limit);

		logger.info("There are {} AuditEntries to be deleted", auditList.size());

		for (AuditEntryEntity audit : auditList) {
			logger.debug("Deleting audit {} with {} auditentries", audit.getId(), audit.getAuditDetails().size());
			dao.delete(audit);
		}
	}

	@Override
	protected BaseDao<AuditEntryEntity, Long> getDao() {
		return dao;
	}
}
