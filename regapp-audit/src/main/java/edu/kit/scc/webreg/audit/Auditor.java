package edu.kit.scc.webreg.audit;

import java.io.Serializable;

import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;

public interface Auditor extends Serializable {

	public abstract AuditEntryEntity getAudit();

	public abstract void startAuditTrail(String executor);

	public abstract void logAction(String subject, String action,
			String object, String log, AuditStatus status);

	public abstract void finishAuditTrail();

	public abstract void setName(String name);

	public abstract void setDetail(String detail);

	public abstract String getActualExecutor();

	void setParent(Auditor auditor);

	void startAuditTrail(String executor, Boolean writeAlways);

	void commitAuditTrail();
}