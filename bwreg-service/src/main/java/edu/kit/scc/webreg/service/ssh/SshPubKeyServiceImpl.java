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

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.dao.SshPubKeyRegistryDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.SshPubKeyEvent;
import edu.kit.scc.webreg.event.SshPubKeyRegistryEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import edu.kit.scc.webreg.ssh.SshPubKeyBlacklistedException;

@Stateless
public class SshPubKeyServiceImpl extends BaseServiceImpl<SshPubKeyEntity, Long> implements SshPubKeyService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private SshPubKeyDao dao;

	@Inject
	private SshPubKeyRegistryDao sshPubKeyRegistryDao;
	
	@Inject
	private EventSubmitter eventSubmitter;
	
	@Override
	public List<SshPubKeyEntity> findByUser(Long userId) {
		return dao.findByUser(userId);
	}
	
	@Override
	public List<SshPubKeyEntity> findByUserAndStatus(Long userId, SshPubKeyStatus keyStatus) {
		return dao.findByUserAndStatus(userId, keyStatus);
	}

	@Override
	public List<SshPubKeyEntity> findByUserAndStatusWithRegs(Long userId, SshPubKeyStatus keyStatus) {
		return dao.findByUserAndStatusWithRegs(userId, keyStatus);
	}
	
	@Override
	public SshPubKeyEntity deleteKey(SshPubKeyEntity entity, String executor) {

		entity = dao.merge(entity);
		entity.setKeyStatus(SshPubKeyStatus.DELETED);

		for (SshPubKeyRegistryEntity regKey : entity.getSshPubKeyRegistries()) {
			sshPubKeyRegistryDao.delete(regKey);
			SshPubKeyRegistryEvent event = new SshPubKeyRegistryEvent(regKey);
			try {
				eventSubmitter.submit(event, EventType.SSH_KEY_REGISTRY_DELETED, executor);
			} catch (EventSubmitException e) {
				logger.warn("Could not submit event", e);
			}
		}
		
		SshPubKeyEvent event = new SshPubKeyEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.SSH_KEY_DELETED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		return entity;
	}

	@Override
	public SshPubKeyEntity deployKey(Long userId, SshPubKeyEntity entity, String executor) 
			throws SshPubKeyBlacklistedException {
		List<SshPubKeyEntity> keyList = dao.findByUserAndKey(userId, entity.getEncodedKey());
		if (keyList != null && keyList.size() > 0) {
			logger.warn("User {} tried to re-add blacklisted key", userId);
			throw new SshPubKeyBlacklistedException("Key already used by user");
		}
		SshPubKeyEvent event = new SshPubKeyEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.SSH_KEY_DEPLOYED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		return dao.persist(entity);
	}
	
	@Override
	protected BaseDao<SshPubKeyEntity, Long> getDao() {
		return dao;
	}
}
