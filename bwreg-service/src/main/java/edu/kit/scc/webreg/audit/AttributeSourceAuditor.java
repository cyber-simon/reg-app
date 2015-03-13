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

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.AuditDetailDao;
import edu.kit.scc.webreg.dao.AuditEntryDao;
import edu.kit.scc.webreg.entity.AuditAttributeSourceEntity;
import edu.kit.scc.webreg.entity.AuditEntryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;

public class AttributeSourceAuditor extends AbstractAuditor {

	private AuditAttributeSourceEntity audit;
	
	public AttributeSourceAuditor(AuditEntryDao auditEntryDao,
			AuditDetailDao auditDetailDao, ApplicationConfig appConfig) {

		super(auditEntryDao, auditDetailDao, appConfig);
	}

	@Override
	public AuditEntryEntity getAudit() {
		if (audit == null)
			audit = new AuditAttributeSourceEntity();
		
		return audit;
	}

	public void setAttributeSource(AttributeSourceEntity entity) {
		audit.setAttributeSource(entity);
	}
	
	public void setUser(UserEntity entity) {
		audit.setUser(entity);
	}

	public void setService(ServiceEntity entity) {
		audit.setService(entity);
	}
}
