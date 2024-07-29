package edu.kit.scc.webreg.service.impl;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.IOException;
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

import org.apache.commons.beanutils.PropertyUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.soap.common.SOAPException;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.slf4j.Logger;
import org.slf4j.MDC;

import edu.kit.scc.webreg.as.AttributeSourceUpdater;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.audit.IdpCommunicationAuditor;
import edu.kit.scc.webreg.audit.RegistryAuditor;
import edu.kit.scc.webreg.audit.UserUpdateAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.SamlAssertionDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrDao;
import edu.kit.scc.webreg.dao.as.AttributeSourceDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.SamlAssertionEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
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
import edu.kit.scc.webreg.hook.HookManager;
import edu.kit.scc.webreg.hook.UserServiceHook;
import edu.kit.scc.webreg.logging.LogHelper;
import edu.kit.scc.webreg.service.attribute.IncomingSamlAttributesHandler;
import edu.kit.scc.webreg.service.group.HomeOrgGroupUpdater;
import edu.kit.scc.webreg.service.identity.IdentityUpdater;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;
import edu.kit.scc.webreg.service.reg.impl.Registrator;
import edu.kit.scc.webreg.service.saml.AttributeQueryHelper;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.exc.MetadataException;
import edu.kit.scc.webreg.service.saml.exc.NoAssertionException;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.service.saml.exc.SamlUnknownPrincipalException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserUpdater extends AbstractUserUpdater<SamlUserEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private Saml2AssertionService saml2AssertionService;

	@Inject
	private AttributeQueryHelper attrQueryHelper;

	@Inject
	private SamlUserDao userDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private HomeOrgGroupUpdater homeOrgGroupUpdater;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private SamlIdpMetadataDao idpDao;

	@Inject
	private SamlSpConfigurationDao spDao;

	@Inject
	private SerialDao serialDao;

	@Inject
	private HookManager hookManager;

	@Inject
	private AttributeSourceDao attributeSourceDao;

	@Inject
	private ASUserAttrDao asUserAttrDao;

	@Inject
	private SamlAssertionDao samlAsserionDao;

	@Inject
	private AttributeSourceUpdater attributeSourceUpdater;

	@Inject
	private AttributeMapHelper attrHelper;

	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private Registrator registrator;

	@Inject
	private IdentityUpdater identityUpdater;

	@Inject
	private IncomingSamlAttributesHandler incomingAttributeHandler;
	
	@Inject
	private LogHelper logHelper;

	@Override
	public SamlUserEntity updateUser(SamlUserEntity user, Map<String, List<Object>> attributeMap, String executor,
			StringBuffer debugLog, String lastLoginHost) throws UserUpdateException {
		return updateUser(user, attributeMap, executor, null, debugLog, lastLoginHost);
	}

	@Override
	public SamlUserEntity updateUser(SamlUserEntity user, Map<String, List<Object>> attributeMap, String executor,
			ServiceEntity service, StringBuffer debugLog, String lastLoginHost) throws UserUpdateException {
		MDC.put("userId", "" + user.getId());
		logger.debug("Updating SAML user {}", user.getEppn());

		boolean changed = false;

		UserUpdateAuditor auditor = new UserUpdateAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-UserUpdate-Audit");
		auditor.setDetail("Update user " + user.getEppn());

		changed |= preUpdateUser(user, attributeMap, user.getIdp().getGenericStore(), executor, service, debugLog);

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
			IncomingAttributeSetEntity incomingAttributeSet = incomingAttributeHandler.createOrUpdateAttributes(user, new HashMap<>());
			incomingAttributeHandler.processIncomingAttributeSet(incomingAttributeSet);

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
		} else {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			user.getGenericStore().put("no_assertion_count", "0");
			user.getGenericStore().put("last_valid_assertion", df.format(new Date()));

			changed |= updateUserFromAttribute(user, attributeMap, auditor);

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

			/*
			 * if service is set, update only attribute sources spcific for this service.
			 * Else update all (login via web or generic attribute query)
			 */
			if (service != null) {
				service = serviceDao.find(equal(ServiceEntity_.id, service.getId()),
						ServiceEntity_.attributeSourceService);

				for (AttributeSourceServiceEntity asse : service.getAttributeSourceService()) {
					changed |= attributeSourceUpdater.updateUserAttributes(user, asse.getAttributeSource(), executor);
				}
			} else {
				// find all user sources to update
				Set<AttributeSourceEntity> asList = new HashSet<>(attributeSourceDao
						.findAll(equal(AttributeSourceEntity_.userSource, true)));
				// and add all sources which are already connected to the user
				asList.addAll(asUserAttrDao.findAll(equal(ASUserAttrEntity_.user, user)).stream()
						.map(a -> a.getAttributeSource()).toList());
				for (AttributeSourceEntity as : asList) {
					changed |= attributeSourceUpdater.updateUserAttributes(user, as, executor);
				}
			}

			Set<GroupEntity> changedGroups = homeOrgGroupUpdater.updateGroupsForUser(user, attributeMap, auditor);

			if (changedGroups.size() > 0) {
				changed = true;
			}

			Map<String, String> attributeStore = user.getAttributeStore();
			attributeStore.clear();
			for (Entry<String, List<Object>> entry : attributeMap.entrySet()) {
				attributeStore.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue()));
			}

			IncomingAttributeSetEntity incomingAttributeSet = incomingAttributeHandler.createOrUpdateAttributes(user, attributeMap);
			incomingAttributeHandler.processIncomingAttributeSet(incomingAttributeSet);
			
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

		changed |= postUpdateUser(user, attributeMap, user.getIdp().getGenericStore(), executor, service, debugLog,
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

	private Date getNextScheduledUpdate() {
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

	public SamlUserEntity updateUser(SamlUserEntity user, Assertion assertion, String executor, ServiceEntity service,
			StringBuffer debugLog, String lastLoginHost) throws UserUpdateException {

		if (assertion != null) {
			samlAsserionDao.deleteAssertionForUser(user);

			SamlAssertionEntity samlAssertionEntity = samlAsserionDao.createNew();
			samlAssertionEntity.setUser(user);
			samlAssertionEntity.setAssertionData(samlHelper.prettyPrint(assertion));
			samlAssertionEntity.setValidUntil(new Date(System.currentTimeMillis() + (4L * 60L * 60L * 1000L)));
			samlAssertionEntity = samlAsserionDao.persist(samlAssertionEntity);
		}

		Map<String, List<Object>> attributeMap = saml2AssertionService.extractAttributes(assertion);

		if (debugLog != null) {
			debugLog.append("Extracted attributes from Assertion:\n");
			for (Entry<String, List<Object>> entry : attributeMap.entrySet()) {
				debugLog.append(entry.getKey()).append(":\t").append(entry.getValue()).append("\n");
			}
		}

		if (service != null)
			return updateUser(user, attributeMap, executor, service, debugLog, lastLoginHost);
		else
			return updateUser(user, attributeMap, executor, debugLog, lastLoginHost);
	}

	public SamlUserEntity updateUser(SamlUserEntity user, Assertion assertion, String executor, String lastLoginHost)
			throws UserUpdateException {

		return updateUser(user, assertion, executor, null, null, lastLoginHost);
	}

	public SamlUserEntity updateUserFromIdp(SamlUserEntity user, String executor) throws UserUpdateException {
		return updateUserFromIdp(user, null, executor, null);
	}

	public SamlUserEntity updateUserFromIdp(SamlUserEntity user, ServiceEntity service, String executor,
			StringBuffer debugLog) throws UserUpdateException {

		SamlSpConfigurationEntity spEntity = spDao.findByEntityId(user.getPersistentSpId());
		SamlIdpMetadataEntity idpEntity = idpDao.findByEntityId(user.getIdp().getEntityId());

		IdpCommunicationAuditor auditor = new IdpCommunicationAuditor(auditDao, auditDetailDao, appConfig);
		auditor.setName("UpdateUserFromIdp");
		auditor.setDetail("Call IDP " + idpEntity.getEntityId() + " from SP " + spEntity.getEntityId() + " for User "
				+ user.getEppn());
		auditor.setIdp(idpEntity);
		auditor.setSpConfig(spEntity);
		auditor.startAuditTrail(executor);

		EntityDescriptor idpEntityDescriptor = samlHelper.unmarshal(idpEntity.getEntityDescriptor(),
				EntityDescriptor.class, auditor);

		Response samlResponse;
		try {
			/*
			 * If something goes wrong here, communication with the idp probably failed
			 */

			samlResponse = attrQueryHelper.query(user, idpEntity, idpEntityDescriptor, spEntity, debugLog);

			if (logger.isTraceEnabled())
				logger.trace("{}", samlHelper.prettyPrint(samlResponse));

			if (debugLog != null) {
				debugLog.append("\nIncoming SAML Response:\n\n").append(samlHelper.prettyPrint(samlResponse))
						.append("\n");
			}

		} catch (SOAPException e) {
			/*
			 * This exception is thrown if the certificate chain is incomplete e.g.
			 */
			handleException(user, e, idpEntity, auditor, debugLog);
			throw new UserUpdateException(e);
		} catch (MetadataException e) {
			/*
			 * is thrown if AttributeQuery location is missing in metadata, or something is
			 * wrong with the sp certificate
			 */
			handleException(user, e, idpEntity, auditor, debugLog);
			throw new UserUpdateException(e);
		} catch (SecurityException e) {
			handleException(user, e, idpEntity, auditor, debugLog);
			throw new UserUpdateException(e);
		} catch (Exception e) {
			handleException(user, e, idpEntity, auditor, debugLog);
			throw new UserUpdateException(e);
		}

		try {
			/*
			 * Don't check Assertion Signature, because we are contacting the IDP directly
			 */
			Assertion assertion;
			try {
				if (debugLog != null) {
					debugLog.append("\nExtracting Assertion from SAML Response without signature check...\n");
				}

				assertion = saml2AssertionService.processSamlResponse(samlResponse, idpEntity, idpEntityDescriptor,
						spEntity, false);

				if (logger.isTraceEnabled())
					logger.trace("{}", samlHelper.prettyPrint(assertion));

			} catch (NoAssertionException e) {
				if (user.getIdp() != null)
					logger.warn("No assertion delivered for user {} from idp {}", user.getEppn(),
							user.getIdp().getEntityId());
				else
					logger.warn("No assertion delivered for user {} from idp {}", user.getEppn());
				assertion = null;
			} catch (SamlUnknownPrincipalException e) {
				if (user.getIdp() != null)
					logger.warn("Unknown principal status for user {} from idp {}", user.getEppn(),
							user.getIdp().getEntityId());
				else
					logger.warn("Unknown principal status  for user {}", user.getEppn());
				assertion = null;
			}

			updateIdpStatus(SamlIdpMetadataEntityStatus.GOOD, idpEntity);

			return updateUser(user, assertion, "attribute-query", service, debugLog, null);
		} catch (DecryptionException e) {
			handleException(user, e, idpEntity, auditor, debugLog);
			throw new UserUpdateException(e);
		} catch (IOException e) {
			handleException(user, e, idpEntity, auditor, debugLog);
			throw new UserUpdateException(e);
		} catch (SamlAuthenticationException e) {
			/*
			 * Thrown if i.e. the AttributeQuery profile is not configured correctly
			 */
			handleException(user, e, idpEntity, auditor, debugLog);
			throw new UserUpdateException(e);
		}
	}

	protected void handleException(SamlUserEntity user, Exception e, SamlIdpMetadataEntity idpEntity, Auditor auditor,
			StringBuffer debugLog) {
		updateFail(user);
		String message = e.getMessage();
		if (e.getCause() != null)
			message += " InnerCause: " + e.getCause().getMessage();
		auditor.logAction(idpEntity.getEntityId(), "SAML ATTRIBUTE QUERY", user.getEppn(), message, AuditStatus.FAIL);
		auditor.finishAuditTrail();
		auditor.commitAuditTrail();

		if (debugLog != null) {
			debugLog.append("Attribute Query failed: ").append(e.getMessage());
			if (e.getCause() != null)
				debugLog.append("Cause: ").append(e.getCause().getMessage());
			debugLog.append(logHelper.convertStacktrace(e));
		}

		updateIdpStatus(SamlIdpMetadataEntityStatus.FAULTY, idpEntity);
	}

	protected void updateIdpStatus(SamlIdpMetadataEntityStatus status, SamlIdpMetadataEntity idpEntity) {
		if (!status.equals(idpEntity.getAqIdpStatus())) {
			idpEntity.setAqIdpStatus(status);
			idpEntity.setLastAqStatusChange(new Date());
		}
	}

	protected void updateFail(SamlUserEntity user) {
		user.setLastFailedUpdate(new Date());
		user.setScheduledUpdate(getNextScheduledUpdate());
		user = userDao.persist(user);
	}

	protected void fireUserChangeEvent(UserEntity user, String executor, Auditor auditor) {

		UserEvent userEvent = new UserEvent(user, auditor.getAudit());

		try {
			eventSubmitter.submit(userEvent, EventType.USER_UPDATE, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
	}

	public boolean updateUserNew(SamlUserEntity user, Map<String, List<Object>> attributeMap, String executor,
			Auditor auditor, StringBuffer debugLog, String lastLoginHost) throws UserUpdateException {
		boolean changed = false;

		changed |= preUpdateUser(user, attributeMap, user.getIdp().getGenericStore(), executor, null, debugLog);
		changed |= updateUserFromAttribute(user, attributeMap, auditor);
		changed |= postUpdateUser(user, attributeMap, user.getIdp().getGenericStore(), executor, null, debugLog,
				lastLoginHost);

		return changed;
	}

	public boolean updateUserFromAttribute(SamlUserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException {
		return updateUserFromAttribute(user, attributeMap, false, auditor);
	}

	public boolean updateUserFromAttribute(SamlUserEntity user, Map<String, List<Object>> attributeMap,
			boolean withoutUidNumber, Auditor auditor) throws UserUpdateException {

		boolean changed = false;

		UserServiceHook completeOverrideHook = null;
		Set<UserServiceHook> activeHooks = new HashSet<UserServiceHook>();

		for (UserServiceHook hook : hookManager.getUserHooks()) {
			if (hook.isResponsible(user, attributeMap)) {

				hook.preUpdateUserFromAttribute(user, attributeMap, auditor);
				activeHooks.add(hook);

				if (hook.isCompleteOverride()) {
					completeOverrideHook = hook;
				}
			}
		}

		if (completeOverrideHook == null) {
			changed |= compareAndChangeProperty(user, "email", attributeMap.get("urn:oid:0.9.2342.19200300.100.1.3"),
					auditor);
			changed |= compareAndChangeProperty(user, "eppn", attributeMap.get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"),
					auditor);
			changed |= compareAndChangeProperty(user, "givenName", attributeMap.get("urn:oid:2.5.4.42"), auditor);
			changed |= compareAndChangeProperty(user, "surName", attributeMap.get("urn:oid:2.5.4.4"), auditor);

			List<String> emailList = attrHelper.attributeListToStringList(attributeMap,
					"urn:oid:0.9.2342.19200300.100.1.3");
			if (emailList != null && emailList.size() > 1) {

				if (user.getEmailAddresses() == null) {
					user.setEmailAddresses(new HashSet<String>());
				}

				for (int i = 1; i < emailList.size(); i++) {
					user.getEmailAddresses().add(emailList.get(i));
				}
			}

			if ((!withoutUidNumber) && (user.getUidNumber() == null)) {
				user.setUidNumber(serialDao.nextUidNumber().intValue());
				logger.info("Setting UID Number {} for user {}", user.getUidNumber(), user.getEppn());
				auditor.logAction(user.getEppn(), "SET FIELD", "uidNumber", "" + user.getUidNumber(),
						AuditStatus.SUCCESS);
				changed = true;
			}
		} else {
			logger.info("Overriding standard User Update Mechanism! Activator: {}",
					completeOverrideHook.getClass().getName());
		}

		for (UserServiceHook hook : activeHooks) {
			hook.postUpdateUserFromAttribute(user, attributeMap, auditor);
		}

		return changed;
	}

	private boolean compareAndChangeProperty(UserEntity user, String property, List<Object> objectValue,
			Auditor auditor) {
		String s = null;
		String action = null;

		// In case of a List (multiple SAML Values), take the first value
		String value = attrHelper.getSingleStringFirst(objectValue);

		try {
			Object actualValue = PropertyUtils.getProperty(user, property);

			if (actualValue != null && actualValue.equals(value)) {
				// Value didn't change, do nothing
				return false;
			}

			if (actualValue == null && value == null) {
				// Value stayed null
				return false;
			}

			if (actualValue == null) {
				s = "null";
				action = "SET FIELD";
			} else {
				s = actualValue.toString();
				action = "UPDATE FIELD";
			}

			s = s + " -> " + value;
			if (s.length() > 1017)
				s = s.substring(0, 1017) + "...";

			PropertyUtils.setProperty(user, property, value);

			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.SUCCESS);
		} catch (IllegalAccessException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		} catch (InvocationTargetException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		} catch (NoSuchMethodException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		}

		return true;
	}

	protected void changeUserStatus(UserEntity user, UserStatus toStatus, Auditor auditor) {
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
}
