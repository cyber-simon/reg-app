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
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.audit.AuditIdpCommunicationEntity;

public class IdpCommunicationAuditor extends AbstractAuditor<AuditIdpCommunicationEntity> {

	private static final long serialVersionUID = 1L;

	public IdpCommunicationAuditor(AuditEntryDao auditEntryDao,
			AuditDetailDao auditDetailDao, ApplicationConfig appConfig) {

		super(auditEntryDao, auditDetailDao, appConfig);
	}

	public void setIdp(SamlIdpMetadataEntity idp) {
		audit.setIdp(idp);
	}

	public void setSpConfig(SamlSpConfigurationEntity spConfig) {
		audit.setSpConfig(spConfig);
	}

	@Override
	protected AuditIdpCommunicationEntity newInstance() {
		return new AuditIdpCommunicationEntity();
	}
}
