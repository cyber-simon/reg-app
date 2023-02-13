package edu.kit.scc.regapp.sshkey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.ServiceEventDao;
import edu.kit.scc.webreg.dao.SshPubKeyRegistryDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.EventEntity;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.SshPubKeyRegistryEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;

@ApplicationScoped
public class SshPubKeyRegistryManager implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private SshPubKeyRegistryDao dao;
	
	@Inject
	private IdentityDao identityDao;

	@Inject
	private ServiceEventDao serviceEventDao;

	@Inject
	private EventSubmitter eventSubmitter;

	public SshPubKeyRegistryEntity deployRegistry(SshPubKeyRegistryEntity entity, String executor, String requestServerName) {
		entity = dao.persist(entity);
		
		SshPubKeyRegistryEvent event = new SshPubKeyRegistryEvent(entity);
		event.setServerName(requestServerName);
		try {
			if (entity.getKeyStatus().equals(SshPubKeyRegistryStatus.PENDING)) {
				eventSubmitter.submit(event, EventType.SSH_KEY_REGISTRY_APPROVAL, executor);

				List<EventEntity> eventList = new ArrayList<EventEntity>(serviceEventDao.findAllByService(entity.getRegistry().getService()));
				eventSubmitter.submit(event, eventList, EventType.SSH_KEY_REGISTRY_APPROVAL, executor);
			}
			else {
				eventSubmitter.submit(event, EventType.SSH_KEY_REGISTRY_DEPLOYED, executor);
				
				List<EventEntity> eventList = new ArrayList<EventEntity>(serviceEventDao.findAllByService(entity.getRegistry().getService()));
				eventSubmitter.submit(event, eventList, EventType.SSH_KEY_REGISTRY_DEPLOYED, executor);
			}
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		return entity;
	}
	
	public SshPubKeyRegistryEntity approveRegistry(SshPubKeyRegistryEntity entity, Long approverId) {
		entity = dao.merge(entity);
		entity.setKeyStatus(SshPubKeyRegistryStatus.ACTIVE);
		entity.setApprover(identityDao.fetch(approverId));
		entity.setApprovedAt(new Date());
		
		SshPubKeyRegistryEvent event = new SshPubKeyRegistryEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.SSH_KEY_REGISTRY_DEPLOYED, "user-" + approverId);

			List<EventEntity> eventList = new ArrayList<EventEntity>(serviceEventDao.findAllByService(entity.getRegistry().getService()));
			eventSubmitter.submit(event, eventList, EventType.SSH_KEY_REGISTRY_DEPLOYED, "user-" + approverId);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		return entity;
	}

	public SshPubKeyRegistryEntity denyRegistry(SshPubKeyRegistryEntity entity, Long approverId) {
		entity = dao.merge(entity);
		entity.setKeyStatus(SshPubKeyRegistryStatus.DENIED);
		entity.setApprover(identityDao.fetch(approverId));
		entity.setApprovedAt(new Date());
		
		SshPubKeyRegistryEvent event = new SshPubKeyRegistryEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.SSH_KEY_REGISTRY_DENIED, "user-" + approverId);

			List<EventEntity> eventList = new ArrayList<EventEntity>(serviceEventDao.findAllByService(entity.getRegistry().getService()));
			eventSubmitter.submit(event, eventList, EventType.SSH_KEY_REGISTRY_DENIED, "user-" + approverId);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		return entity;
	}

	public void deleteRegistry(SshPubKeyRegistryEntity entity, String executor, String requestServerName) {
		
		dao.delete(entity);
		
		SshPubKeyRegistryEvent event = new SshPubKeyRegistryEvent(entity);
		event.setServerName(requestServerName);
		try {
			eventSubmitter.submit(event, EventType.SSH_KEY_REGISTRY_DELETED, executor);

			List<EventEntity> eventList = new ArrayList<EventEntity>(serviceEventDao.findAllByService(entity.getRegistry().getService()));
			eventSubmitter.submit(event, eventList, EventType.SSH_KEY_REGISTRY_DELETED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
	}

	
}
