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
package edu.kit.scc.webreg.service.ssh;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import edu.kit.scc.regapp.sshkey.SshPubKeyRegistryManager;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SshPubKeyRegistryDao;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity_;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryStatus;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class SshPubKeyRegistryServiceImpl extends BaseServiceImpl<SshPubKeyRegistryEntity>
		implements SshPubKeyRegistryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SshPubKeyRegistryDao dao;

	@Inject
	private HttpServletRequest request;

	@Inject
	SshPubKeyRegistryManager manager;

	@Override
	public List<SshPubKeyRegistryEntity> findByRegistry(Long registryId) {
		return dao.findAll(equal("registry.id", registryId));
	}

	@Override
	public List<SshPubKeyRegistryEntity> findForApproval(Long serviceId) {
		return dao.findAll(and(equal("registry.service.id", serviceId),
				equal(SshPubKeyRegistryEntity_.keyStatus, SshPubKeyRegistryStatus.PENDING)));
	}

	@Override
	public SshPubKeyRegistryEntity deployRegistry(SshPubKeyRegistryEntity entity, String executor) {
		return manager.deployRegistry(entity, executor, request.getServerName());
	}

	@Override
	public SshPubKeyRegistryEntity approveRegistry(SshPubKeyRegistryEntity entity, Long approverId) {
		return manager.approveRegistry(entity, approverId);
	}

	@Override
	public SshPubKeyRegistryEntity denyRegistry(SshPubKeyRegistryEntity entity, Long approverId) {
		return manager.denyRegistry(entity, approverId);
	}

	@Override
	public void deleteRegistry(SshPubKeyRegistryEntity entity, String executor) {
		manager.deleteRegistry(entity, executor, request.getServerName());
	}

	@Override
	protected BaseDao<SshPubKeyRegistryEntity> getDao() {
		return dao;
	}
}
