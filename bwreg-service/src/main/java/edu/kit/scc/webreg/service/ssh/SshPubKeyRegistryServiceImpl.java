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
import edu.kit.scc.webreg.dao.SshPubKeyRegistryDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.SshPubKeyRegistryEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class SshPubKeyRegistryServiceImpl extends BaseServiceImpl<SshPubKeyRegistryEntity, Long> implements SshPubKeyRegistryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private SshPubKeyRegistryDao dao;
	
	@Inject
	private EventSubmitter eventSubmitter;

	@Override
	public List<SshPubKeyRegistryEntity> findByUserAndService(Long userId, Long serviceId) {
		return dao.findByUserAndService(userId, serviceId);
	}
	
	@Override
	public List<SshPubKeyRegistryEntity> findByRegistry(Long registryId) {
		return dao.findByRegistry(registryId);
	}
	
	@Override
	public SshPubKeyRegistryEntity deployRegistry(SshPubKeyRegistryEntity entity, String executor) {
		entity = dao.persist(entity);
		
		SshPubKeyRegistryEvent event = new SshPubKeyRegistryEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.SSH_KEY_REGISTRY_DEPLOYED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		return entity;
	}
	
	@Override
	public void deleteRegistry(SshPubKeyRegistryEntity entity, String executor) {
		
		dao.delete(entity);
		
		SshPubKeyRegistryEvent event = new SshPubKeyRegistryEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.SSH_KEY_REGISTRY_DELETED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
	}

	@Override
	protected BaseDao<SshPubKeyRegistryEntity, Long> getDao() {
		return dao;
	}
}
