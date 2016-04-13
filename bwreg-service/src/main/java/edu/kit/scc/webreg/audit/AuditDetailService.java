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

import java.util.List;

import edu.kit.scc.webreg.entity.audit.AuditDetailEntity;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.service.BaseService;

public interface AuditDetailService extends BaseService<AuditDetailEntity, Long> {

	List<AuditDetailEntity> findNewestFailed(int limit);

	List<AuditDetailEntity> findAllByAuditEntry(AuditEntryEntity auditEntry);

}
