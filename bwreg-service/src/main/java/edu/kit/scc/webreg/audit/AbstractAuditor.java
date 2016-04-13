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
import java.util.HashSet;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.audit.AuditDetailEntity;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;

public abstract class AbstractAuditor implements Auditor {

	protected static final String WRITE_EMPTY_AUDITS = "AbstractAuditor.writeEmptyAudits";
	
	protected AuditEntryDao auditEntryDao;
	protected AuditDetailDao auditDetailDao;
	protected ApplicationConfig appConfig;
	
	public AbstractAuditor(AuditEntryDao auditEntryDao, AuditDetailDao auditDetailDao, ApplicationConfig appConfig) {
		this.auditDetailDao = auditDetailDao;
		this.auditEntryDao = auditEntryDao;
		this.appConfig = appConfig;
	}

	@Override
	public abstract AuditEntryEntity getAudit();
	
	@Override
	public void setParent(Auditor auditor) {
		if (auditor != null)
			getAudit().setParentEntry(auditor.getAudit());
	}
	
	@Override
	public void startAuditTrail(String executor) {
		getAudit().setStartTime(new Date());
		getAudit().setAuditDetails(new HashSet<AuditDetailEntity>());
		getAudit().setExecutor(executor);
	}

	@Override
	public void logAction(String subject, String action, String object, String log, AuditStatus status) {
		AuditDetailEntity detail = auditDetailDao.createNew();

		if (log != null && log.length() > 1023)
			log = log.substring(0, 1018) + "...";
		if (subject != null && subject.length() > 255)
			subject = subject.substring(0, 250) + "...";
		if (action != null && action.length() > 127)
			action = action.substring(0, 123) + "...";
		if (object != null && object.length() > 255)
			object = object.substring(0, 250) + "...";
		
		detail.setSubject(subject);
		detail.setAction(action);
		detail.setObject(object);
		detail.setLog(log);
		detail.setAuditStatus(status);
		detail.setEndTime(new Date());
		detail.setAuditEntry(getAudit());
		getAudit().getAuditDetails().add(detail);
	}
	
	@Override
	public void finishAuditTrail() {
		if (getAudit().getAuditDetails().size() > 0 ||
				getWriteEmptyAudits()) {
			getAudit().setEndTime(new Date());
			auditEntryDao.persist(getAudit());
		}
	}
	
	@Override
	public void setName(String name) {
		getAudit().setName(name);
	}
	
	@Override
	public void setDetail(String detail) {
		if (detail != null && detail.length() > 510)
			detail = detail.substring(0, 508) + "...";
		getAudit().setDetail(detail);
	}
	
	@Override
	public String getActualExecutor() {
		if (getAudit() != null)
			return getAudit().getExecutor();
		else
			return null;
	}
	
	protected boolean getWriteEmptyAudits() {
		Boolean wea = Boolean.parseBoolean(appConfig.getConfigValue(WRITE_EMPTY_AUDITS));
		return wea;
	}
}
