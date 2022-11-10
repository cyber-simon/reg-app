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

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import edu.kit.scc.regapp.sshkey.SshPubKeyManager;
import edu.kit.scc.regapp.sshkey.exc.SshPubKeyBlacklistedException;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class SshPubKeyServiceImpl extends BaseServiceImpl<SshPubKeyEntity> implements SshPubKeyService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SshPubKeyDao dao;

	@Inject
	private SshPubKeyManager manager;
	
	@Inject 
	private HttpServletRequest request;
	
	@Override
	public List<SshPubKeyEntity> findByIdentity(Long identityId) {
		return dao.findByIdentity(identityId);
	}

	@Override
	public List<SshPubKeyEntity> findByKey(String encodedKey) {
		return dao.findByKey(encodedKey);
	}
	
	@Override
	public List<SshPubKeyEntity> findByIdentityAndStatus(Long identityId, SshPubKeyStatus keyStatus) {
		return dao.findByIdentityAndStatus(identityId, keyStatus);
	}

	@Override
	public List<SshPubKeyEntity> findByIdentityAndStatusWithRegs(Long identityId, SshPubKeyStatus keyStatus) {
		return dao.findByIdentityAndStatusWithRegs(identityId, keyStatus);
	}

	@Override
	public List<SshPubKeyEntity> findKeysToExpire(int limit) {
		return dao.findKeysToExpire(limit);
	}

	@Override
	public List<SshPubKeyEntity> findKeysToDelete(int limit, int days) {
		return dao.findKeysToDelete(limit, days);
	}
	
	@Override
	public List<SshPubKeyEntity> findKeysToExpiryWarning(int limit, int days) {
		return dao.findKeysToExpiryWarning(limit, days);
	}
	
	@Override
	public SshPubKeyEntity expireKey(SshPubKeyEntity entity, String executor) {
		return manager.expireKey(entity, executor);
	}

	@Override
	public SshPubKeyEntity expiryWarningKey(SshPubKeyEntity entity, String executor) {
		return manager.expiryWarningKey(entity, executor);
	}

	@Override
	public SshPubKeyEntity keyExpirySent(SshPubKeyEntity entity) {
		return manager.keyExpirySent(entity);
	}
	
	@Override
	public SshPubKeyEntity keyExpiryWarningSent(SshPubKeyEntity entity) {
		return manager.keyExpiryWarningSent(entity);
	}
	
	@Override
	public SshPubKeyEntity deleteKey(SshPubKeyEntity entity, String executor) {
		return manager.deleteKey(entity, executor, request.getServerName());
	}

	@Override
	public SshPubKeyEntity deployKey(Long identityId, SshPubKeyEntity entity, String executor) 
			throws SshPubKeyBlacklistedException {
		return manager.deployKey(identityId, entity, executor, request.getServerName());
	}
	
	@Override
	protected BaseDao<SshPubKeyEntity> getDao() {
		return dao;
	}
}
