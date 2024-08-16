package edu.kit.scc.webreg.service.impl;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.MDC;

import edu.kit.scc.webreg.as.AttributeSourceUpdater;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.audit.RegistryAuditor;
import edu.kit.scc.webreg.audit.UserUpdateAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrDao;
import edu.kit.scc.webreg.dao.as.AttributeSourceDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceEntity_;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity_;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity_;
import edu.kit.scc.webreg.entity.as.AttributeSourceServiceEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import edu.kit.scc.webreg.entity.audit.AuditDetailEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.entity.audit.AuditUserUpdateEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.UserEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.hook.IdentityScriptingHookWorkflow;
import edu.kit.scc.webreg.hook.UserUpdateHook;
import edu.kit.scc.webreg.hook.UserUpdateHookException;
import edu.kit.scc.webreg.service.attribute.IncomingAttributesHandler;
import edu.kit.scc.webreg.service.group.HomeOrgGroupUpdater;
import edu.kit.scc.webreg.service.identity.IdentityScriptingEnv;
import edu.kit.scc.webreg.service.identity.IdentityUpdater;
import edu.kit.scc.webreg.service.reg.impl.Registrator;
import jakarta.inject.Inject;

public abstract class AbstractUserUpdater<T extends UserEntity> implements UserUpdater<T>, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private IdentityUpdater identityUpdater;

	@Inject
	private IdentityScriptingEnv scriptingEnv;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private AttributeSourceDao attributeSourceDao;

	@Inject
	private ASUserAttrDao asUserAttrDao;

	@Inject
	private AttributeSourceUpdater attributeSourceUpdater;

	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private AttributeMapHelper attrHelper;

	@Inject
	private Registrator registrator;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private EventSubmitter eventSubmitter;

	public abstract boolean updateUserFromAttribute(T user, Map<String, List<Object>> attributeMap,
			boolean withoutUidNumber, Auditor auditor) throws UserUpdateException;

	public abstract Map<String, String> resolveHomeOrgGenericStore(T user);

	public abstract IncomingAttributesHandler<?> resolveIncomingAttributeHandler(T user);

	public boolean updateUserFromAttribute(T user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException {
		return updateUserFromAttribute(user, attributeMap, false, auditor);
	}

	@Override
	public T expireUser(T user, String executor) {
		logger.info("Expiring user {}. Trying one last update", user.getId());

		UserUpdateAuditor auditor = new UserUpdateAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-UserExpire-Audit");
		auditor.setDetail("Expire user " + user.getId());

		try {
			user = updateUserFromHomeOrg(user, null, executor, null);

			// User update from home org did not fail. That means, we don't need to bother
			// the user to login. Clear the expiry warning, things should be done
			// automatically
			user.setExpireWarningSent(null);

			return user;

		} catch (UserUpdateException e) {
			// The Exception is expected, because the home org will not accept user updates
			// in the back channel. The user already got an expire warning at this point.
			user.getAttributeStore().clear();

			// user empty attribute map in order to remove all existing values
			IncomingAttributeSetEntity incomingAttributeSet = resolveIncomingAttributeHandler(user)
					.createOrUpdateAttributes(user, new HashMap<>());
			resolveIncomingAttributeHandler(user).processIncomingAttributeSet(incomingAttributeSet);

			// sets user account on ON_HOLD, if it's in state ACTIVE
			deactivateUser(user, auditor);

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			user.getGenericStore().put("epired_on", df.format(new Date()));

			return user;
		} finally {
			auditor.setUser(user);
			auditor.finishAuditTrail();
			auditor.commitAuditTrail();
		}
	}

	public T updateUser(T user, Map<String, List<Object>> attributeMap, String executor, StringBuffer debugLog,
			String lastLoginHost) throws UserUpdateException {
		return updateUser(user, attributeMap, executor, null, debugLog, lastLoginHost);
	}

	public T updateUser(T user, Map<String, List<Object>> attributeMap, String executor, ServiceEntity service,
			StringBuffer debugLog, String lastLoginHost) throws UserUpdateException {
		MDC.put("userId", "" + user.getId());
		logger.debug("Updating user {} (class: {})", user.getId(), user.getClass().getSimpleName());

		boolean changed = false;

		UserUpdateAuditor auditor = new UserUpdateAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-UserUpdate-Audit");
		auditor.setDetail("Update user " + user.getId());

		changed |= preUpdateUser(user, attributeMap, resolveHomeOrgGenericStore(user), executor, service, debugLog);

		// List to store parent services, that are not registered. Need to be registered
		// later, when attribute map is populated
		List<ServiceEntity> delayedRegisterList = new ArrayList<ServiceEntity>();

		/**
		 * put no_assertion_count in generic store if assertion is missing. Else reset
		 * no assertion count and put last valid assertion date in
		 */
		if (attributeMap == null) {
			if (!user.getGenericStore().containsKey("no_assertion_count")) {
				user.getGenericStore().put("no_assertion_count", "1");
			} else {
				user.getGenericStore().put("no_assertion_count",
						"" + (Long.parseLong(user.getGenericStore().get("no_assertion_count")) + 1L));
			}

			logger.info("No attribute for user {}, skipping updateFromAttribute", user.getEppn());

			user.getAttributeStore().clear();

			// user empty attribute map in order to remove all existing values
			IncomingAttributeSetEntity incomingAttributeSet = resolveIncomingAttributeHandler(user)
					.createOrUpdateAttributes(user, new HashMap<>());
			resolveIncomingAttributeHandler(user).processIncomingAttributeSet(incomingAttributeSet);

			// sets user account on ON_HOLD, if it's in state ACTIVE
			deactivateUser(user, auditor);

		} else {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			user.getGenericStore().put("no_assertion_count", "0");
			user.getGenericStore().put("last_valid_assertion", df.format(new Date()));

			changed |= updateUserFromAttribute(user, attributeMap, auditor);

			// if a user is in state ON_HOLD, this reactivates the user to ACTIVE
			// and sets all registries to LOST_ACCESS in order to be checked again
			changed |= reactivateUser(user, delayedRegisterList, auditor);

			changed |= updateAttributeSources(user, service, executor, auditor);

			changed |= updateGroups(user, attributeMap, auditor);

			Map<String, String> attributeStore = user.getAttributeStore();
			attributeStore.clear();
			for (Entry<String, List<Object>> entry : attributeMap.entrySet()) {
				attributeStore.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue()));
			}

			IncomingAttributeSetEntity incomingAttributeSet = resolveIncomingAttributeHandler(user)
					.createOrUpdateAttributes(user, attributeMap);
			resolveIncomingAttributeHandler(user).processIncomingAttributeSet(incomingAttributeSet);

			identityUpdater.updateIdentity(user);

			if (appConfig.getConfigValue("create_missing_eppn_scope") != null) {
				if (user.getEppn() == null) {
					String scope = appConfig.getConfigValue("create_missing_eppn_scope");
					user.setEppn(user.getIdentity().getGeneratedLocalUsername() + "@" + scope);
					changed = true;
				}
			}
		}

		for (ServiceEntity delayedService : delayedRegisterList) {
			try {
				registrator.registerUser(user, delayedService, "user-" + user.getId(), false);
			} catch (RegisterException e) {
				logger.warn("Parent registration didn't work out like it should", e);
			}
		}

		changed |= postUpdateUser(user, attributeMap, resolveHomeOrgGenericStore(user), executor, service, debugLog,
				lastLoginHost);

		user.setLastUpdate(new Date());
		user.setLastFailedUpdate(null);
		user.setExpireWarningSent(null);
		user.setExpiredSent(null);
		user.setScheduledUpdate(getNextScheduledUpdate());

		if (changed) {
			fireUserChangeEvent(user, auditor.getActualExecutor(), auditor);
		}

		auditor.setUser(user);
		auditor.finishAuditTrail();
		auditor.commitAuditTrail();

		if (debugLog != null) {
			AuditUserUpdateEntity audit = auditor.getAudit();
			debugLog.append("\n\nPrinting audit from user update process:\n\nName: ").append(audit.getName())
					.append("\nDetail: ").append(audit.getDetail()).append("\n");
			for (AuditDetailEntity detail : audit.getAuditDetails()) {
				debugLog.append(detail.getEndTime()).append(" | ").append(detail.getSubject()).append(" | ")
						.append(detail.getObject()).append(" | ").append(detail.getAction()).append(" | ")
						.append(detail.getLog()).append(" | ").append(detail.getAuditStatus()).append("\n");
			}

			if (audit.getAuditDetails().size() == 0) {
				debugLog.append("Nothing seems to have changed.\n");
			}
		}

		return user;
	}

	public abstract HomeOrgGroupUpdater<T> getGroupUpdater();

	protected boolean preUpdateUser(T user, Map<String, List<Object>> attributeMap,
			Map<String, String> homeOrgGenericStore, String executor, ServiceEntity service, StringBuffer debugLog)
			throws UserUpdateException {

		boolean returnValue = false;

		UserUpdateHook updateHook = resolveUpdateHook(homeOrgGenericStore);

		if (updateHook != null) {
			try {
				returnValue |= updateHook.preUpdateUser(user, homeOrgGenericStore, attributeMap, executor, service,
						null);
			} catch (UserUpdateHookException e) {
				logger.warn("An exception happened while calling UserUpdateHook!", e);
			}
		}

		return returnValue;
	}

	protected boolean postUpdateUser(T user, Map<String, List<Object>> attributeMap,
			Map<String, String> homeOrgGenericStore, String executor, ServiceEntity service, StringBuffer debugLog,
			String lastLoginHost) throws UserUpdateException {

		boolean returnValue = false;

		if (lastLoginHost != null) {
			user.setLastLoginHost(lastLoginHost);
		}

		UserUpdateHook updateHook = resolveUpdateHook(homeOrgGenericStore);

		if (updateHook != null) {
			try {
				returnValue |= updateHook.postUpdateUser(user, homeOrgGenericStore, attributeMap, executor, service,
						null);
			} catch (UserUpdateHookException e) {
				logger.warn("An exception happened while calling UserUpdateHook!", e);
			}
		}
		return returnValue;
	}

	private UserUpdateHook resolveUpdateHook(Map<String, String> homeOrgGenericStore) {
		UserUpdateHook updateHook = null;
		if (homeOrgGenericStore.containsKey("user_update_hook")) {
			String hookClass = homeOrgGenericStore.get("user_update_hook");
			try {
				updateHook = (UserUpdateHook) Class.forName(hookClass).getDeclaredConstructor().newInstance();
				if (updateHook instanceof IdentityScriptingHookWorkflow)
					((IdentityScriptingHookWorkflow) updateHook).setScriptingEnv(scriptingEnv);

			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				logger.warn("Cannot instantiate updateHook class. This is probably a misconfiguration.");
			}
		}

		return updateHook;
	}

	protected void deactivateUser(T user, Auditor auditor) {
		if (UserStatus.ACTIVE.equals(user.getUserStatus())) {
			changeUserStatus(user, UserStatus.ON_HOLD, auditor);

			identityUpdater.updateIdentity(user);

			/*
			 * Also flag all registries for user ON_HOLD
			 */
			List<RegistryEntity> registryList = registryDao.findByUserAndStatus(user, RegistryStatus.ACTIVE,
					RegistryStatus.LOST_ACCESS, RegistryStatus.INVALID);
			for (RegistryEntity registry : registryList) {
				changeRegistryStatus(registry, RegistryStatus.ON_HOLD, "user-on-hold", auditor);
			}
		}
	}

	protected boolean reactivateUser(T user, List<ServiceEntity> delayedRegisterList, Auditor auditor) {
		Boolean changed = false;
		if (UserStatus.ON_HOLD.equals(user.getUserStatus())) {
			changeUserStatus(user, UserStatus.ACTIVE, auditor);

			/*
			 * Also reenable all registries for user to LOST_ACCESS. They are rechecked then
			 */
			List<RegistryEntity> registryList = registryDao.findByUserAndStatus(user, RegistryStatus.ON_HOLD);
			for (RegistryEntity registry : registryList) {
				changeRegistryStatus(registry, RegistryStatus.LOST_ACCESS, "user-reactivated", auditor);

				/*
				 * check if parent registry is missing
				 */
				if (registry.getService().getParentService() != null) {
					List<RegistryEntity> parentRegistryList = registryDao.findByServiceAndIdentityAndNotStatus(
							registry.getService().getParentService(), user.getIdentity(), RegistryStatus.DELETED,
							RegistryStatus.DEPROVISIONED);
					if (parentRegistryList.size() == 0) {
						delayedRegisterList.add(registry.getService().getParentService());
					}
				}
			}

			/*
			 * fire a user changed event to be sure, when the user is activated
			 */
			changed = true;
		}

		return changed;
	}

	protected boolean updateGroups(T user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException {
		Set<GroupEntity> changedGroups = getGroupUpdater().updateGroupsForUser(user, attributeMap, auditor);

		if (changedGroups.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean updateAttributeSources(T user, ServiceEntity service, String executor, Auditor auditor)
			throws UserUpdateException {
		Boolean changed = false;

		/*
		 * if service is set, update only attribute sources spcific for this service.
		 * Else update all (login via web or generic attribute query)
		 */
		if (service != null) {
			service = serviceDao.find(equal(ServiceEntity_.id, service.getId()), ServiceEntity_.attributeSourceService);

			for (AttributeSourceServiceEntity asse : service.getAttributeSourceService()) {
				changed |= attributeSourceUpdater.updateUserAttributes(user, asse.getAttributeSource(), executor);
			}
		} else {
			// find all user sources to update
			Set<AttributeSourceEntity> asList = new HashSet<>(
					attributeSourceDao.findAll(equal(AttributeSourceEntity_.userSource, true)));
			// and add all sources which are already connected to the user
			asList.addAll(asUserAttrDao.findAll(equal(ASUserAttrEntity_.user, user)).stream()
					.map(a -> a.getAttributeSource()).toList());
			for (AttributeSourceEntity as : asList) {
				changed |= attributeSourceUpdater.updateUserAttributes(user, as, executor);
			}
		}
		return changed;
	}

	protected void changeUserStatus(T user, UserStatus toStatus, Auditor auditor) {
		UserStatus fromStatus = user.getUserStatus();
		user.setUserStatus(toStatus);
		user.setLastStatusChange(new Date());

		logger.debug("{}: change user status from {} to {}", user.getEppn(), fromStatus, toStatus);
		auditor.logAction(user.getEppn(), "CHANGE STATUS", fromStatus + " -> " + toStatus,
				"Change status " + fromStatus + " -> " + toStatus, AuditStatus.SUCCESS);
	}

	protected void changeRegistryStatus(RegistryEntity registry, RegistryStatus toStatus, String statusMessage,
			Auditor parentAuditor) {
		RegistryStatus fromStatus = registry.getRegistryStatus();
		registry.setRegistryStatus(toStatus);
		registry.setStatusMessage(statusMessage);
		registry.setLastStatusChange(new Date());

		logger.debug("{} {} {}: change registry status from {} to {}", new Object[] { registry.getUser().getEppn(),
				registry.getService().getShortName(), registry.getId(), fromStatus, toStatus });
		RegistryAuditor registryAuditor = new RegistryAuditor(auditDao, auditDetailDao, appConfig);
		registryAuditor.setParent(parentAuditor);
		registryAuditor.startAuditTrail(parentAuditor.getActualExecutor());
		registryAuditor.setName(getClass().getName() + "-UserUpdate-Registry-Audit");
		registryAuditor.setDetail("Update registry " + registry.getId() + " for user " + registry.getUser().getEppn());
		registryAuditor.setRegistry(registry);
		registryAuditor.logAction(registry.getUser().getEppn(), "CHANGE STATUS", "registry-" + registry.getId(),
				"Change status " + fromStatus + " -> " + toStatus, AuditStatus.SUCCESS);
		registryAuditor.finishAuditTrail();
	}

	protected Date getNextScheduledUpdate() {
		Long futureMillis = 30L * 24L * 60L * 60L * 1000L;
		if (appConfig.getConfigOptions().containsKey("update_schedule_future")) {
			futureMillis = Long.decode(appConfig.getConfigValue("update_schedule_future"));
		}
		Integer futureMillisRandom = 6 * 60 * 60 * 1000;
		if (appConfig.getConfigOptions().containsKey("update_schedule_future_random")) {
			futureMillisRandom = Integer.decode(appConfig.getConfigValue("update_schedule_future_random"));
		}
		Random r = new Random();
		return new Date(System.currentTimeMillis() + futureMillis + r.nextInt(futureMillisRandom));
	}

	protected void fireUserChangeEvent(T user, String executor, Auditor auditor) {

		UserEvent userEvent = new UserEvent(user, auditor.getAudit());

		try {
			eventSubmitter.submit(userEvent, EventType.USER_UPDATE, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
	}
}
