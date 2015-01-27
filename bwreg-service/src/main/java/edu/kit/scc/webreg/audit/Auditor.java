package edu.kit.scc.webreg.audit;

import edu.kit.scc.webreg.entity.AuditEntryEntity;
import edu.kit.scc.webreg.entity.AuditStatus;

public interface Auditor {

	public abstract AuditEntryEntity getAudit();

	public abstract void startAuditTrail(String executor);

	public abstract void logAction(String subject, String action,
			String object, String log, AuditStatus status);

	public abstract void finishAuditTrail();

	public abstract void setName(String name);

	public abstract void setDetail(String detail);

	public abstract String getActualExecutor();
}