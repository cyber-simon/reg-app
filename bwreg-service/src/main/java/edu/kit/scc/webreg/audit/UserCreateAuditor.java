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

import java.util.Map.Entry;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.entity.audit.AuditUserCreateEntity;

public class UserCreateAuditor extends AbstractAuditor<AuditUserCreateEntity> {

	private static final long serialVersionUID = 1L;

	public UserCreateAuditor(AuditEntryDao auditEntryDao,
			AuditDetailDao auditDetailDao, ApplicationConfig appConfig) {

		super(auditEntryDao, auditDetailDao, appConfig);
	}

	public void setUser(UserEntity entity) {
		audit.setUser(entity);
	}
	
	public void auditUserCreate() {
		UserEntity user = audit.getUser();

		for (Entry<String, String> entry : user.getAttributeStore().entrySet()) {
			String s = entry.getValue();
			if (s.length() > 1017) s = s.substring(0, 1017) + "...";			
			logAction(user.getEppn(), "SET SAML ATTRIBUTE", entry.getKey(),
					entry.getValue(), AuditStatus.SUCCESS);			
		}
	}

	@Override
	protected AuditUserCreateEntity newInstance() {
		return new AuditUserCreateEntity();
	}
}
