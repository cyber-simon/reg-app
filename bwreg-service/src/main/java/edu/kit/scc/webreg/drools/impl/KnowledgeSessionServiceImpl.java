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
package edu.kit.scc.webreg.drools.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.ServiceRegisterAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.AuditDetailDao;
import edu.kit.scc.webreg.dao.AuditEntryDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.AuditStatus;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.ServiceRegisterEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.MisconfiguredApplicationException;
import edu.kit.scc.webreg.exc.MisconfiguredServiceException;

@Stateless
public class KnowledgeSessionServiceImpl implements KnowledgeSessionService {

	@Inject
	private Logger logger;
	
	@Inject
	private RegistryDao registryDao;
	
	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private UserDao userDao;

	@Inject
	private ServiceDao serviceDao;
	
	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private ApplicationConfig appConfig;
	
	@Override
	public KieSession getStatefulSession(String packageName, String knowledgeBaseName, String knowledgeBaseVersion) {
		
		KieServices ks = KieServices.Factory.get();
		ReleaseId releaseId = ks.newReleaseId(packageName, knowledgeBaseName, knowledgeBaseVersion);
		
		return getStatefulSession(ks, releaseId);
	}

	@Override
	public KieSession getStatefulSession(String unitId) {
		String[] splits = unitId.split(":");
		
		if (splits.length != 3)
			throw new IllegalArgumentException("unitId must contain two :");
		
		return getStatefulSession(splits[0], splits[1], splits[2]);
	}
	
	protected KieSession getStatefulSession(KieServices ks, ReleaseId releaseId) {
		KieContainer kc = ks.newKieContainer(releaseId);
		return kc.newKieSession();
	}

	@Override
	public List<Object> checkRule(String unitId, UserEntity user, Map<String, List<Object>> attributeMap,
				Assertion assertion, SamlIdpMetadataEntity idp, EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity sp) 
			throws MisconfiguredServiceException {
		
		KieSession ksession = getStatefulSession(unitId);

		if (ksession == null)
			throw new MisconfiguredApplicationException("Es ist keine valide Regel fuer den Benutzer zugriff konfiguriert");

		ksession.setGlobal("logger", logger);
		ksession.insert(user);
		ksession.insert(attributeMap);
		ksession.insert(assertion);
		ksession.insert(idp);
		ksession.insert(idpEntityDescriptor);
		ksession.insert(sp);
		ksession.insert(new Date());
		
		ksession.fireAllRules();

		List<Object> objectList = new ArrayList<Object>(ksession.getObjects());

		for (Object o : objectList) {
			logger.debug("Deleting fact handle for Object {}", o);
			FactHandle factHandle = ksession.getFactHandle(o);
			if (factHandle != null)
				ksession.delete(factHandle);
			else
				logger.warn("Facthandle for Object {} is null", o);
		}

		ksession.dispose();

		return objectList;
	}
	
	@Override
	public List<Object> checkRule(String packageName, String knowledgeBaseName, String knowledgeBaseVersion, 
			UserEntity user, ServiceEntity service,
			RegistryEntity registry, String executor) 
			throws MisconfiguredServiceException {
		return checkRule(packageName, knowledgeBaseName, knowledgeBaseVersion, user, service, registry, executor, true);
	}
	
	@Override
	public List<Object> checkRule(String packageName, String knowledgeBaseName, String knowledgeBaseVersion, 
			UserEntity user, ServiceEntity service,
			RegistryEntity registry, String executor, Boolean withCache) 
			throws MisconfiguredServiceException {

		user = userDao.findById(user.getId());
		
		if (withCache) {
			service = serviceDao.findById(service.getId());

			// Default expiry Time after which an registry is checked
			Long expireTime = 10000L;
			
			if (service.getServiceProps() != null && service.getServiceProps().containsKey("access_check_expire_time")) {
				expireTime = Long.parseLong(service.getServiceProps().get("access_check_expire_time"));
			}
			
			if (registry.getLastAccessCheck() != null &&
					(System.currentTimeMillis() - registry.getLastAccessCheck().getTime()) < expireTime) {
				logger.info("Skipping access check for user {} and service {}", new Object[] {user.getEppn(), 
						service.getName()});
				return null;
			}
		}
		
		KieSession ksession = getStatefulSession(packageName, knowledgeBaseName, knowledgeBaseVersion);

		if (ksession == null)
			throw new MisconfiguredServiceException("Der Registrierungsprozess für den Dienst ist nicht korrekt konfiguriert (Keine Zugangsregel geladen)");

		ksession.setGlobal("logger", logger);
		
		ksession.insert(user);
		ksession.insert(service);
		ksession.insert(registry);
		ksession.insert(new Date());
		
		logger.debug("Test all Rules for service {}", service.getName());
		ksession.fireAllRules();

		List<Object> objectList = new ArrayList<Object>(ksession.getObjects());

		for (Object o : objectList) {
			logger.debug("Deleting fact handle for Object {}", o);
			FactHandle factHandle = ksession.getFactHandle(o);
			if (factHandle != null)
				ksession.delete(factHandle);
			else
				logger.warn("Facthandle for Object {} is null", o);
		}
		
		ksession.dispose();
		
		if (registry != null) {
			ServiceRegisterEvent serviceRegisterEvent = new ServiceRegisterEvent(registry);

			if (RegistryStatus.LOST_ACCESS.equals(registry.getRegistryStatus())) {
				if (hasAccess(objectList)) {
					registry.setRegistryStatus(RegistryStatus.ACTIVE);
					registry.setLastStatusChange(new Date());
					auditAccessChange(user, service, registry, false, executor);
					try {
						eventSubmitter.submit(serviceRegisterEvent, EventType.USER_GAINED_ACCESS, executor);
					} catch (EventSubmitException e) {
						logger.warn("Could not submit event", e);
					}
				}
			}
			else {
				if (! hasAccess(objectList)) {
					registry.setRegistryStatus(RegistryStatus.LOST_ACCESS);
					registry.setLastStatusChange(new Date());
					auditAccessChange(user, service, registry, true, executor);
					try {
						eventSubmitter.submit(serviceRegisterEvent, EventType.USER_LOST_ACCESS, executor);
					} catch (EventSubmitException e) {
						logger.warn("Could not submit event", e);
					}
				}
			}
			
			registry.setLastAccessCheck(new Date());
			registry = registryDao.persist(registry);
		}
		
		return objectList;
	}

	@Override
	public Map<RegistryEntity, List<Object>> checkRules(List<RegistryEntity> registryList, UserEntity user, String executor) {
		return checkRules(registryList, user, executor, true);
	}
	
	@Override
	public Map<RegistryEntity, List<Object>> checkRules(List<RegistryEntity> registryList, UserEntity user, 
			String executor, Boolean withCache) {
		
		Map<RegistryEntity, List<Object>> returnMap = new HashMap<RegistryEntity, List<Object>>();
		
		user = userDao.findById(user.getId());

		for (RegistryEntity registry : registryList) {
			ServiceEntity service = registry.getService();
			
			List<Object> objectList;
			
			if (service.getAccessRule() == null) {
				objectList = checkRule("default", "permitAllRule", "1.0.0", user, service, registry, executor, withCache);
			}
			else {
				BusinessRulePackageEntity rulePackage = service.getAccessRule().getRulePackage();
				objectList = checkRule(rulePackage.getPackageName(), rulePackage.getKnowledgeBaseName(), 
						rulePackage.getKnowledgeBaseVersion(), user, service, registry, executor, withCache);
			}

			returnMap.put(registry, objectList);
		}
		
		return returnMap;
	}
	
	private boolean hasAccess(List<Object> objectList) {
		
		for (Object o : objectList) {
			if (o instanceof OverrideAccess) {
				return true;
			}
		}
		
		for (Object o : objectList) {
			if (o instanceof UnauthorizedUser) {
				return false;
			}
		}
		return true;
	}
	
	private void auditAccessChange(UserEntity user, ServiceEntity service,
			RegistryEntity registry, boolean lostAccess, String executor) {
		ServiceRegisterAuditor auditor = new ServiceRegisterAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-CheckRule-Audit");
		if (lostAccess) {
			auditor.setDetail("User " + user.getEppn() + " lost Access for service " + service.getName());
			auditor.logAction(user.getEppn(), "LOST ACCESS", service.getName(), "User " + user.getEppn() + " lost Access for service " + service.getName(), AuditStatus.SUCCESS);
		}
		else {
			auditor.setDetail("User " + user.getEppn() + " gained Access for service " + service.getName());
			auditor.logAction(user.getEppn(), "GAINED ACCESS", service.getName(), "User " + user.getEppn() + " gained Access for service " + service.getName(), AuditStatus.SUCCESS);
		}
		auditor.setRegistry(registry);

		auditor.finishAuditTrail();
	}
}
