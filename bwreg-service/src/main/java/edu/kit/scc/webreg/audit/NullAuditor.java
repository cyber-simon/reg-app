package edu.kit.scc.webreg.audit;

import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;

public class NullAuditor implements Auditor {

	private String executor;
	
	@Override
	public AuditEntryEntity getAudit() {
		return null;
	}

	@Override
	public void startAuditTrail(String executor) {
		this.executor = executor;
	}

	@Override
	public void logAction(String subject, String action, String object,
			String log, AuditStatus status) {
	}

	@Override
	public void finishAuditTrail() {

	}

	@Override
	public void setName(String name) {
	}

	@Override
	public void setDetail(String detail) {
	}

	@Override
	public String getActualExecutor() {
		return executor;
	}

	@Override
	public void setParent(Auditor auditor) {
	}

}
