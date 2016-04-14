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
package edu.kit.scc.webreg.service.reg;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface ApprovalService {

	void registerApproval(RegistryEntity registry, Auditor auditor) throws RegisterException;

	void approve(RegistryEntity registry, String executor, Auditor auditor) throws RegisterException;

	void denyApproval(RegistryEntity registry, String executor, Auditor auditor)
			throws RegisterException;

	void approve(RegistryEntity registry, String executor,
			Boolean sendGroupUpdate, Auditor auditor) throws RegisterException;
	
}
