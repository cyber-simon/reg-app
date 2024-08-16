package edu.kit.scc.webreg.service.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.soap.common.SOAPException;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.audit.IdpCommunicationAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.SamlAssertionDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.SamlAssertionEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingSamlAttributeEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.hook.HookManager;
import edu.kit.scc.webreg.hook.UserServiceHook;
import edu.kit.scc.webreg.logging.LogHelper;
import edu.kit.scc.webreg.service.attribute.IncomingAttributesHandler;
import edu.kit.scc.webreg.service.attribute.IncomingSamlAttributesHandler;
import edu.kit.scc.webreg.service.group.HomeOrgGroupUpdater;
import edu.kit.scc.webreg.service.group.SamlGroupUpdater;
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
public class SamlUserUpdater extends AbstractUserUpdater<SamlUserEntity> {

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
	private SamlGroupUpdater homeOrgGroupUpdater;

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
	private IncomingSamlAttributesHandler incomingAttributeHandler;

	@Inject
	private SamlAssertionDao samlAsserionDao;

	@Inject
	private AttributeMapHelper attrHelper;

	@Inject ApplicationConfig appConfig;

	@Inject
	private LogHelper logHelper;

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
		return updateUserFromHomeOrg(user, null, executor, null);
	}

	public SamlUserEntity updateUserFromHomeOrg(SamlUserEntity user, ServiceEntity service, String executor,
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



	public boolean updateUserNew(SamlUserEntity user, Map<String, List<Object>> attributeMap, String executor,
			Auditor auditor, StringBuffer debugLog, String lastLoginHost) throws UserUpdateException {
		boolean changed = false;

		changed |= preUpdateUser(user, attributeMap, user.getIdp().getGenericStore(), executor, null, debugLog);
		changed |= updateUserFromAttribute(user, attributeMap, auditor);
		changed |= postUpdateUser(user, attributeMap, user.getIdp().getGenericStore(), executor, null, debugLog,
				lastLoginHost);

		return changed;
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

	@Override
	public HomeOrgGroupUpdater<SamlUserEntity> getGroupUpdater() {
		return homeOrgGroupUpdater;
	}

	@Override
	public Map<String, String> resolveHomeOrgGenericStore(SamlUserEntity user) {
		return user.getIdp().getGenericStore();
	}

	@Override
	public IncomingAttributesHandler<IncomingSamlAttributeEntity> resolveIncomingAttributeHandler(SamlUserEntity user) {
		return incomingAttributeHandler;
	}
}
