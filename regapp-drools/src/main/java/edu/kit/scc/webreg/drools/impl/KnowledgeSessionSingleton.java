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
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.ServiceRegisterAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
import edu.kit.scc.webreg.drools.DroolsConfigurationException;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.ServiceRegisterEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.MisconfiguredApplicationException;
import edu.kit.scc.webreg.exc.MisconfiguredServiceException;

@Named
@ApplicationScoped
public class KnowledgeSessionSingleton {

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
	private IdentityDao identityDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private ApplicationConfig appConfig;

	public KieSession getStatefulSession(String packageName, String knowledgeBaseName, String knowledgeBaseVersion) {

		KieServices ks = KieServices.Factory.get();
		ReleaseId releaseId = ks.newReleaseId(packageName, knowledgeBaseName, knowledgeBaseVersion);

		return getStatefulSession(ks, releaseId);
	}

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

	public List<ServiceEntity> checkServiceFilterRule(String unitId, IdentityEntity identity,
			List<ServiceEntity> serviceList, Set<GroupEntity> groups, Set<RoleEntity> roles,
			Set<ProjectMembershipEntity> projects) throws DroolsConfigurationException {

		KieSession ksession = getStatefulSession(unitId);

		if (ksession == null)
			throw new DroolsConfigurationException("Es ist keine valide Regel fuer den Benutzerzugriff konfiguriert");

		ksession.setGlobal("logger", logger);
		ksession.insert(identity);
		for (UserEntity user : identity.getUsers())
			ksession.insert(user);
		for (GroupEntity group : groups)
			ksession.insert(group);
		for (ServiceEntity service : serviceList)
			ksession.insert(service);
		for (ProjectMembershipEntity project : projects)
			ksession.insert(project);
		ksession.insert(new Date());

		ksession.fireAllRules();

		List<Object> objectList = new ArrayList<Object>(ksession.getObjects());
		List<ServiceEntity> removeList = new ArrayList<ServiceEntity>();

		for (Object o : objectList) {
			FactHandle factHandle = ksession.getFactHandle(o);
			if (factHandle != null)
				ksession.delete(factHandle);

			if (o instanceof ServiceEntity) {
				removeList.add((ServiceEntity) o);
			}
		}

		ksession.dispose();

		List<ServiceEntity> returnList = new ArrayList<ServiceEntity>(serviceList);
		returnList.removeAll(removeList);

		return returnList;
	}

	public List<Object> checkIdentityRule(BusinessRulePackageEntity rulePackage, IdentityEntity identity)
			throws MisconfiguredServiceException {
		KieSession ksession = getStatefulSession(rulePackage.getPackageName(), rulePackage.getKnowledgeBaseName(),
				rulePackage.getKnowledgeBaseVersion());

		if (ksession == null)
			throw new MisconfiguredApplicationException(
					"Es ist keine valide Regel fuer den Benutzerzugriff konfiguriert");

		ksession.setGlobal("logger", logger);
		ksession.insert(identity);
		ksession.insert(new Date());

		ksession.fireAllRules();

		List<Object> objectList = new ArrayList<Object>(ksession.getObjects());

		for (Object o : objectList) {
			if (logger.isTraceEnabled())
				logger.trace("Deleting fact handle for Object {}", o);
			FactHandle factHandle = ksession.getFactHandle(o);
			if (factHandle != null)
				ksession.delete(factHandle);
			else
				logger.warn("Facthandle for Object {} is null", o);
		}

		ksession.dispose();

		return objectList;

	}

	public List<String> checkScriptAccess(ScriptEntity scriptEntity, IdentityEntity identity) {
		ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());
		List<String> unauthorizedList = new ArrayList<String>();

		if (engine == null) {
			logger.warn("No engine set for script {}. Allowing access", scriptEntity.getName());
			return unauthorizedList;
		}

		try {
			engine.eval(scriptEntity.getScript());

			Invocable invocable = (Invocable) engine;

			invocable.invokeFunction("checkAccess", identity, unauthorizedList, logger);

		} catch (ScriptException e) {
			logger.warn("Script execution failed.", e);
		} catch (NoSuchMethodException e) {
			logger.info("No checkAccess method in script. Allowing access");
		}
		return unauthorizedList;
	}

	public List<Object> checkSamlLoginRule(String unitId, UserEntity user, Map<String, List<Object>> attributeMap,
			Assertion assertion, SamlIdpMetadataEntity idp, EntityDescriptor idpEntityDescriptor,
			SamlSpConfigurationEntity sp) throws MisconfiguredServiceException {

		KieSession ksession = getStatefulSession(unitId);

		if (ksession == null)
			throw new MisconfiguredApplicationException(
					"Es ist keine valide Regel fuer den Benutzerzugriff konfiguriert");

		ksession.setGlobal("logger", logger);
		if (user != null)
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
			if (logger.isTraceEnabled())
				logger.trace("Deleting fact handle for Object {}", o);
			FactHandle factHandle = ksession.getFactHandle(o);
			if (factHandle != null)
				ksession.delete(factHandle);
			else
				logger.warn("Facthandle for Object {} is null", o);
		}

		ksession.dispose();

		return objectList;
	}

	/**
	 * Filters all available services for a UserEntity
	 *
	 * @deprecated use checkServiceFilterRule with IdentityEntity instead.
	 */
	@Deprecated
	public List<ServiceEntity> checkServiceFilterRule(String unitId, UserEntity user, List<ServiceEntity> serviceList,
			Set<GroupEntity> groups, Set<RoleEntity> roles, Set<ProjectMembershipEntity> projects)
			throws MisconfiguredServiceException {

		user = userDao.merge(user);

		KieSession ksession = getStatefulSession(unitId);

		if (ksession == null)
			throw new MisconfiguredApplicationException(
					"Es ist keine valide Regel fuer den Benutzerzugriff konfiguriert");

		ksession.setGlobal("logger", logger);
		ksession.insert(user);
		for (GroupEntity group : groups)
			ksession.insert(group);
		for (ServiceEntity service : serviceList)
			ksession.insert(service);
		ksession.insert(new Date());

		ksession.fireAllRules();

		List<Object> objectList = new ArrayList<Object>(ksession.getObjects());
		List<ServiceEntity> removeList = new ArrayList<ServiceEntity>();

		for (Object o : objectList) {
			if (logger.isTraceEnabled())
				logger.trace("Deleting fact handle for Object {}", o);

			FactHandle factHandle = ksession.getFactHandle(o);
			if (factHandle != null)
				ksession.delete(factHandle);
			else
				logger.warn("Facthandle for Object {} is null", o);

			if (o instanceof ServiceEntity) {
				removeList.add((ServiceEntity) o);
			}
		}

		ksession.dispose();

		List<ServiceEntity> returnList = new ArrayList<ServiceEntity>(serviceList);
		returnList.removeAll(removeList);

		return returnList;
	}

	public List<Object> checkServiceAccessRule(UserEntity user, ServiceEntity service, RegistryEntity registry,
			String executor) throws MisconfiguredServiceException {
		return checkServiceAccessRule(user, service, registry, executor, true);
	}

	public List<Object> checkServiceAccessRule(UserEntity user, ServiceEntity service, String executor,
			Boolean withCache) throws MisconfiguredServiceException {
		return checkServiceAccessRule(user, service, null, executor, withCache);
	}

	public List<Object> checkServiceAccessRule(UserEntity user, ServiceEntity service, RegistryEntity registry,
			String executor, Boolean withCache) throws MisconfiguredServiceException {

		String packageName = "default";
		String knowledgeBaseName = "permitAllRule";
		String knowledgeBaseVersion = "1.0.0";

		if (service.getAccessRule() != null) {
			BusinessRulePackageEntity rulePackage = service.getAccessRule().getRulePackage();

			if (rulePackage == null) {
				throw new IllegalStateException("checkServiceAccess called with a rule ("
						+ service.getAccessRule().getName() + ") that has no rulePackage");
			}

			packageName = rulePackage.getPackageName();
			knowledgeBaseName = rulePackage.getKnowledgeBaseName();
			knowledgeBaseVersion = rulePackage.getKnowledgeBaseVersion();
		}

		if (withCache) {
			// Default expiry Time after which an registry is checked
			Long expireTime = 10000L;

			if (service.getServiceProps() != null
					&& service.getServiceProps().containsKey("access_check_expire_time")) {
				expireTime = Long.parseLong(service.getServiceProps().get("access_check_expire_time"));
			}

			if (registry.getLastAccessCheck() != null
					&& (System.currentTimeMillis() - registry.getLastAccessCheck().getTime()) < expireTime) {
				logger.info("Skipping access check for user {} and service {}",
						new Object[] { user.getEppn(), service.getName() });
				return null;
			}
		}

		List<ProjectMembershipEntity> projects = projectDao.findByIdentity(user.getIdentity());

		KieSession ksession = getStatefulSession(packageName, knowledgeBaseName, knowledgeBaseVersion);

		if (ksession == null)
			throw new MisconfiguredServiceException(
					"Der Registrierungsprozess für den Dienst ist nicht korrekt konfiguriert (Keine Zugangsregel geladen)");

		ksession.setGlobal("logger", logger);

		ksession.insert(user);
		ksession.insert(service);
		ksession.insert(registry);
		for (UserGroupEntity uge : user.getGroups())
			ksession.insert(uge.getGroup());
		for (ProjectMembershipEntity project : projects)
			ksession.insert(project);

		ksession.insert(projects);
		ksession.insert(user.getGroups());
		ksession.insert(new Date());

		logger.debug("Test all Rules for service {}", service.getName());
		ksession.fireAllRules();

		List<Object> objectList = new ArrayList<Object>(ksession.getObjects());

		for (Object o : objectList) {
			if (logger.isTraceEnabled())
				logger.trace("Deleting fact handle for Object {}", o);
			FactHandle factHandle = ksession.getFactHandle(o);
			if (factHandle != null)
				ksession.delete(factHandle);
			else
				logger.warn("Facthandle for Object {} is null", o);
		}

		ksession.dispose();

		if (registry != null) {
			ServiceRegisterEvent serviceRegisterEvent = new ServiceRegisterEvent(registry);

			if (RegistryStatus.LOST_ACCESS.equals(registry.getRegistryStatus())
					|| RegistryStatus.ON_HOLD.equals(registry.getRegistryStatus())) {
				if (hasAccess(objectList)) {
					logger.debug("{} {} {}: registry status changed from {} to ACTIVE", new Object[] { user.getEppn(),
							service.getShortName(), registry.getId(), registry.getRegistryStatus() });
					registry.setRegistryStatus(RegistryStatus.ACTIVE);
					registry.setStatusMessage(null);
					registry.setLastStatusChange(new Date());
					auditAccessChange(user, service, registry, false, executor);
					try {
						eventSubmitter.submit(serviceRegisterEvent, EventType.USER_GAINED_ACCESS, executor);
					} catch (EventSubmitException e) {
						logger.warn("Could not submit event", e);
					}
				} else {
					logger.debug("{} {} {}: stays in status {}", new Object[] { user.getEppn(), service.getShortName(),
							registry.getId(), registry.getRegistryStatus() });
				}
			} else if (RegistryStatus.PENDING.equals(registry.getRegistryStatus())) {
				logger.debug("{} {} {}: stays in status {}", new Object[] { user.getEppn(), service.getShortName(),
						registry.getId(), registry.getRegistryStatus() });
			} else {
				if (!hasAccess(objectList)) {
					logger.debug("{} {} {}: registry status changed from {} to LOST_ACCESS", new Object[] {
							user.getEppn(), service.getShortName(), registry.getId(), registry.getRegistryStatus() });
					registry.setRegistryStatus(RegistryStatus.LOST_ACCESS);
					registry.setStatusMessage(accessToString(objectList));
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
		}

		return objectList;
	}

	public Map<RegistryEntity, List<Object>> checkRules(List<RegistryEntity> registryList, UserEntity user,
			String executor) {
		return checkRules(registryList, user, executor, true);
	}

	public Map<RegistryEntity, List<Object>> checkRules(List<RegistryEntity> registryList, UserEntity user,
			String executor, Boolean withCache) {

		Map<RegistryEntity, List<Object>> returnMap = new HashMap<RegistryEntity, List<Object>>();

		user = userDao.fetch(user.getId());

		for (RegistryEntity registry : registryList) {
			ServiceEntity service = registry.getService();

			List<Object> objectList = checkServiceAccessRule(user, service, registry, executor, withCache);

			returnMap.put(registry, objectList);
		}

		return returnMap;
	}

	public Map<RegistryEntity, List<Object>> checkRules(List<RegistryEntity> registryList, IdentityEntity identity,
			String executor, Boolean withCache) {

		Map<RegistryEntity, List<Object>> returnMap = new HashMap<RegistryEntity, List<Object>>();

		for (RegistryEntity registry : registryList) {
			ServiceEntity service = registry.getService();

			List<Object> objectList = checkServiceAccessRule(registry.getUser(), service, registry, executor,
					withCache);

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

	private String accessToString(List<Object> objectList) {
		StringBuffer sb = new StringBuffer();
		for (Object o : objectList) {
			if (o instanceof UnauthorizedUser) {
				sb.append(((UnauthorizedUser) o).getMessage());
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	private void auditAccessChange(UserEntity user, ServiceEntity service, RegistryEntity registry, boolean lostAccess,
			String executor) {
		ServiceRegisterAuditor auditor = new ServiceRegisterAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-CheckRule-Audit");
		if (lostAccess) {
			auditor.setDetail("User " + user.getEppn() + " lost Access for service " + service.getName());
			auditor.logAction(user.getEppn(), "LOST ACCESS", service.getName(),
					"User " + user.getEppn() + " lost Access for service " + service.getName(), AuditStatus.SUCCESS);
		} else {
			auditor.setDetail("User " + user.getEppn() + " gained Access for service " + service.getName());
			auditor.logAction(user.getEppn(), "GAINED ACCESS", service.getName(),
					"User " + user.getEppn() + " gained Access for service " + service.getName(), AuditStatus.SUCCESS);
		}
		auditor.setRegistry(registry);

		auditor.finishAuditTrail();
		auditor.commitAuditTrail();
	}
}
