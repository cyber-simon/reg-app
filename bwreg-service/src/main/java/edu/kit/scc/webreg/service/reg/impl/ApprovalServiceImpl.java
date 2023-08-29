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
package edu.kit.scc.webreg.service.reg.impl;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.ApprovalService;

@Stateless
public class ApprovalServiceImpl implements ApprovalService {

	@Inject
	private RegistryDao regsitryDao;
	
	@Inject
	private Approvor approvor;
		
	@Override
	public void registerApproval(RegistryEntity registry, Auditor parentAuditor) throws RegisterException {
		registry = regsitryDao.fetch(registry.getId());
		approvor.registerApproval(registry, parentAuditor);
	}

	@Override
	public void denyApproval(RegistryEntity registry, String executor, Auditor parentAuditor) throws RegisterException {
		registry = regsitryDao.fetch(registry.getId());
		approvor.denyApproval(registry, executor, parentAuditor);
	}

	@Override
	public void approve(RegistryEntity registry, String executor, Auditor parentAuditor)
			throws RegisterException {
		registry = regsitryDao.fetch(registry.getId());
		approvor.approve(registry, executor, true, parentAuditor);
	}
	
	@Override
	public void approve(RegistryEntity registry, String executor, Boolean sendGroupUpdate, Auditor parentAuditor)
			throws RegisterException {
		registry = regsitryDao.fetch(registry.getId());
		approvor.approve(registry, executor, sendGroupUpdate, parentAuditor);
	}
}
