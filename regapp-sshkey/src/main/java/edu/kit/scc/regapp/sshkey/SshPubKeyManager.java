package edu.kit.scc.regapp.sshkey;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.regapp.sshkey.exc.SshPubKeyBlacklistedException;
import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.dao.SshPubKeyRegistryDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.SshPubKeyEvent;
import edu.kit.scc.webreg.event.SshPubKeyRegistryEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;

@ApplicationScoped
public class SshPubKeyManager implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private SshPubKeyRegistryDao sshPubKeyRegistryDao;

	@Inject
	private SshPubKeyDao dao;

	@Inject
	private EventSubmitter eventSubmitter;

	public SshPubKeyEntity expireKey(SshPubKeyEntity entity, String executor) {
		entity = dao.merge(entity);
		entity.setKeyStatus(SshPubKeyStatus.EXPIRED);
		logger.debug("Setting key {} to expired", entity.getId());

		for (SshPubKeyRegistryEntity regKey : entity.getSshPubKeyRegistries()) {
			logger.debug("Deleting registry connection {} for key {}", regKey.getId(), entity.getId());
			sshPubKeyRegistryDao.delete(regKey);
		}

		SshPubKeyEvent event = new SshPubKeyEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.SSH_KEY_EXPIRED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		return entity;
	}

	public SshPubKeyEntity expiryWarningKey(SshPubKeyEntity entity, String executor) {
		entity = dao.merge(entity);
		logger.debug("Send expiry warning event for key {}", entity.getId());

		SshPubKeyEvent event = new SshPubKeyEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.SSH_KEY_EXPIRY_WARNING, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		return entity;
	}

	public SshPubKeyEntity keyExpirySent(SshPubKeyEntity entity) {
		entity = dao.merge(entity);
		entity.setExpiredSent(new Date());
		return entity;
	}
	
	public SshPubKeyEntity keyExpiryWarningSent(SshPubKeyEntity entity) {
		entity = dao.merge(entity);
		entity.setExpireWarningSent(new Date());
		return entity;
	}
	
	public SshPubKeyEntity deleteKey(SshPubKeyEntity entity, String executor, String requestServerName) {

		entity = dao.merge(entity);
		entity.setKeyStatus(SshPubKeyStatus.DELETED);
		logger.debug("Setting key {} to deleted", entity.getId());

		for (SshPubKeyRegistryEntity regKey : entity.getSshPubKeyRegistries()) {
			sshPubKeyRegistryDao.delete(regKey);
			logger.debug("Deleting registry connection {} for key {}", regKey.getId(), entity.getId());
			SshPubKeyRegistryEvent event = new SshPubKeyRegistryEvent(regKey);
			event.setServerName(requestServerName);
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

	public SshPubKeyEntity deployKey(Long identityId, SshPubKeyEntity entity, String executor, String requestServerName) 
			throws SshPubKeyBlacklistedException {
		List<SshPubKeyEntity> keyList = dao.findByKey(entity.getEncodedKey());
		if (keyList != null && keyList.size() > 0) {
			logger.warn("User {} tried to re-add blacklisted key", identityId);
			throw new SshPubKeyBlacklistedException("Key already used by user");
		}
		SshPubKeyEvent event = new SshPubKeyEvent(entity);
		event.setServerName(requestServerName);
		try {
			eventSubmitter.submit(event, EventType.SSH_KEY_DEPLOYED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		return dao.persist(entity);
	}
}
