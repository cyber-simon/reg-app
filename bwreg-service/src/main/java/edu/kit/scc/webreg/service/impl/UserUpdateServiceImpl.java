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
package edu.kit.scc.webreg.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.ws.soap.common.SOAPException;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.security.SecurityException;
import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.UserUpdateAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.AuditDetailDao;
import edu.kit.scc.webreg.dao.AuditEntryDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.UserEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.MetadataException;
import edu.kit.scc.webreg.exc.NoAssertionException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.service.HomeOrgGroupService;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.UserUpdateService;
import edu.kit.scc.webreg.service.saml.AttributeQueryHelper;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.SamlHelper;

@Stateless
public class UserUpdateServiceImpl implements UserUpdateService {

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
	private UserService userService;
	
	@Inject
	private HomeOrgGroupService homeOrgGroupService;

	@Inject
	private SamlHelper samlHelper;
		
	@Inject 
	private SamlIdpMetadataService idpService;
	
	@Inject 
	private SamlSpConfigurationService spService;
	
	@Inject
	private AttributeMapHelper attrHelper;

	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private ApplicationConfig appConfig;
	
	@Override
	public UserEntity updateUser(UserEntity user, Map<String, List<Object>> attributeMap, String executor)
			throws RegisterException {
		logger.debug("Updating user {}", user.getEppn());

		boolean changed = false;
		
		UserUpdateAuditor auditor = new UserUpdateAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-UserUpdate-Audit");
		auditor.setDetail("Update user " + user.getEppn());
	
		/**
		 * put no_assertion_count in generic store if assertion is missing. Else
		 * reset no assertion count and put last valid assertion date in
		 */
		if (attributeMap == null) {
			if (! user.getGenericStore().containsKey("no_assertion_count")) {
				user.getGenericStore().put("no_assertion_count", "1");
			}
			else {
				user.getGenericStore().put("no_assertion_count", 
						"" + (Long.parseLong(user.getGenericStore().get("no_assertion_count")) + 1L));
			}
			
			logger.info("No attribute for user {}, skipping updateFromAttribute", user.getEppn());
			
			user.getAttributeStore().clear();

			if (UserStatus.ACTIVE.equals(user.getUserStatus())) {
				user.setUserStatus(UserStatus.ON_HOLD);
				user.setLastStatusChange(new Date());
			}
		}
		else {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			user.getGenericStore().put("no_assertion_count", "0");
			user.getGenericStore().put("last_valid_assertion", df.format(new Date()));
		
			changed |= userService.updateUserFromAttribute(user, attributeMap, auditor);
			
			if (UserStatus.ON_HOLD.equals(user.getUserStatus())) {
				user.setUserStatus(UserStatus.ACTIVE);
				user.setLastStatusChange(new Date());
			}
		
			Set<GroupEntity> changedGroups = homeOrgGroupService.updateGroupsForUser(user, attributeMap, auditor);

			if (changedGroups.size() > 0) {
				changed = true;
			}
			
			Map<String, String> attributeStore = user.getAttributeStore();
			attributeStore.clear();
			for (Entry<String, List<Object>> entry : attributeMap.entrySet()) {
				attributeStore.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue()));
			}
		}
		
		user.setLastUpdate(new Date());
		user.setLastFailedUpdate(null);
		user.setGroups(null);
		
		user = userService.save(user);

		if (changed) {
			fireUserChangeEvent(user, auditor.getActualExecutor());
		}
		
		auditor.setUser(user);
		auditor.finishAuditTrail();
		
		return user;
	}
	
	@Override
	public UserEntity updateUser(UserEntity user, Assertion assertion, String executor)
			throws RegisterException {
		Map<String, List<Object>> attributeMap = saml2AssertionService.extractAttributes(assertion);

		return updateUser(user, attributeMap, executor);
	}
	
	@Override
	public UserEntity updateUserFromIdp(UserEntity user) 
			throws RegisterException {

		SamlSpConfigurationEntity spEntity = spService.findByEntityId(user.getPersistentSpId());
		SamlIdpMetadataEntity idpEntity = idpService.findByEntityId(user.getIdp().getEntityId());
		
		EntityDescriptor idpEntityDescriptor = samlHelper.unmarshal(
				idpEntity.getEntityDescriptor(), EntityDescriptor.class);
		
		Response samlResponse;
		try {
			/*
			 * If something goes wrong here, communication with the idp probably failed
			 */
			
			samlResponse = attrQueryHelper.query(user, idpEntity, idpEntityDescriptor, spEntity);
		} catch (SOAPException e) {
			/*
			 * This exception is thrown if the certificate chain is incomplete e.g.
			 */
			updateFail(user, e);
			throw new RegisterException(e);
		} catch (MetadataException e) {
			/*
			 * is thrown if AttributeQuery location is missing in metadata, or something is wrong
			 * with the sp certificate
			 */
			updateFail(user, e);
			throw new RegisterException(e);
		} catch (SecurityException e) {
			updateFail(user, e);
			throw new RegisterException(e);
		}

		try {
			/*
			 * Don't check Assertion Signature, because we are contacting the IDP directly
			 */
			Assertion assertion;
			try {
				assertion = saml2AssertionService.processSamlResponse(samlResponse, idpEntity, 
						idpEntityDescriptor, spEntity, false);
			} catch (NoAssertionException e) {
				if (user.getIdp() != null)
					logger.warn("No assertion delivered for user {} from idp {}", user.getEppn(), user.getIdp().getEntityId());
				else
					logger.warn("No assertion delivered for user {} from idp {}", user.getEppn());
				assertion = null;
			}

			return updateUser(user, assertion, "attribute-query");
		} catch (DecryptionException e) {
			updateFail(user, e);
			throw new RegisterException(e);
		} catch (IOException e) {
			updateFail(user, e);
			throw new RegisterException(e);
		} catch (SamlAuthenticationException e) {
			/*
			 * Thrown if i.e. the AttributeQuery profile is not configured correctly
			 */
			updateFail(user, e);
			throw new RegisterException(e);
		}
	}
	
	protected void updateFail(UserEntity user, Exception e) {
		user.setLastFailedUpdate(new Date());
		user.setGroups(null);
		user = userService.save(user);
	}

	protected void fireUserChangeEvent(UserEntity user, String executor) {
		
		UserEvent userEvent = new UserEvent(user);
		
		try {
			eventSubmitter.submit(userEvent, EventType.USER_UPDATE, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
	}
	
}
