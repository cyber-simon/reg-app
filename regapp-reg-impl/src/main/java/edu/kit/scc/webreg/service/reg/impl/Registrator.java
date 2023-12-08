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
package edu.kit.scc.webreg.service.reg.impl;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.audit.GroupAuditor;
import edu.kit.scc.webreg.audit.NullAuditor;
import edu.kit.scc.webreg.audit.RegistryAuditor;
import edu.kit.scc.webreg.audit.ServiceRegisterAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.PolicyDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.ServiceEventDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.dao.SshPubKeyRegistryDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.drools.MissingMandatoryValues;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.AgreementTextEntity;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.EventEntity;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.GroupEntity_;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.audit.AuditDetailEntity;
import edu.kit.scc.webreg.entity.audit.AuditServiceRegisterEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.event.ServiceRegisterEvent;
import edu.kit.scc.webreg.event.SshPubKeyRegistryEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.reg.GroupCapable;
import edu.kit.scc.webreg.service.reg.GroupUtil;
import edu.kit.scc.webreg.service.reg.PasswordUtil;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.ScriptingWorkflow;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class Registrator implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private ServiceGroupFlagDao groupFlagDao;

	@Inject
	private ServiceEventDao serviceEventDao;

	@Inject
	private UserDao userDao;

	@Inject
	private GroupDao groupDao;

	@Inject
	private GroupUtil groupUtil;

	@Inject
	private PasswordUtil passwordUtil;

	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private SshPubKeyRegistryDao sshPubKeyRegistryDao;

	@Inject
	private KnowledgeSessionSingleton knowledgeSessionService;

	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private ScriptingEnv scriptingEnv;

	@Inject
	private PolicyDao policyDao;

	@Inject
	private Approvor approvor;

	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor)
			throws RegisterException {
		return registerUser(user, service, executor, true);
	}

	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor, Boolean sendGroupUpdate)
			throws RegisterException {
		return registerUser(user, service, executor, null, sendGroupUpdate, null);
	}

	public RegistryEntity registerUser(UserEntity user, ServiceEntity service, String executor,
			List<Long> policiesIdList, Boolean sendGroupUpdate, Auditor parentAuditor) throws RegisterException {

		if (!UserStatus.ACTIVE.equals(user.getUserStatus())) {
			logger.warn("Only Users in status ACTIVE can register with a service. User {} ({}) is {}", user.getEppn(),
					user.getId(), user.getUserStatus());
			throw new RegisterException("Only Users in status ACTIVE can register with a service");
		}

		ServiceRegisterAuditor auditor = new ServiceRegisterAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor, true);
		auditor.setName(this.getClass().getName() + "-ServiceRegister-Audit");
		auditor.setDetail("Register user " + user.getEppn() + " for service " + service.getName());
		auditor.setParent(parentAuditor);

		if (service.getParentService() != null) {
			logger.info("Service has Parent. Checking parent first.");
			List<RegistryEntity> r = registryDao.findByServiceAndIdentityAndNotStatus(service.getParentService(),
					user.getIdentity(), RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
			if (r.size() == 0) {
				logger.info("User {} is not registered with parent service {} yet", user.getEppn(),
						service.getParentService().getName());
				registerUser(user, service.getParentService(), executor, null, true, auditor);
			} else {
				logger.debug("User {} is already registered with parent service {}", user.getEppn(),
						service.getParentService().getName());
			}
		}

		RegistryEntity registry = registryDao.createNew();

		try {
			ApproverRoleEntity approverRole = service.getApproverRole();

			Set<AgreementTextEntity> agrs = new HashSet<AgreementTextEntity>();
			if (policiesIdList == null) {
				for (PolicyEntity policy : service.getPolicies())
					agrs.add(policy.getActualAgreement());
			} else {
				for (Long policyId : policiesIdList) {
					PolicyEntity policy = policyDao.fetch(policyId);
					if (policy != null && policy.getActualAgreement() != null)
						agrs.add(policyDao.fetch(policyId).getActualAgreement());
					else
						throw new RegisterException("no such policy");
				}
			}
			registry.setAgreedTexts(agrs);
			registry.setAgreedTime(new Date());

			registry.setService(service);
			registry.setUser(user);
			registry.setIdentity(user.getIdentity());
			registry.setRegisterBean(service.getRegisterBean());

			if (approverRole != null)
				registry.setApprovalBean(approverRole.getApprovalBean());

			registry.setRegistryValues(new HashMap<String, String>());

			registry.setRegistryStatus(RegistryStatus.CREATED);
			registry.setLastStatusChange(new Date());

			registry = registryDao.persist(registry);

			auditor.logAction(user.getEppn(), "CREATED REGISTRY", "registry-" + registry.getId(), "Registry is created",
					AuditStatus.SUCCESS);
			auditor.setRegistry(registry);

			if (registry.getApprovalBean() != null) {
				logger.debug("Registering {} for approval {}", user.getEppn(), registry.getApprovalBean());
				auditor.logAction(user.getEppn(), "STARTING APPROVAL", "registry-" + registry.getId(),
						"Approval is started: " + registry.getApprovalBean(), AuditStatus.SUCCESS);
				auditor.finishAuditTrail();
				if (parentAuditor == null)
					auditor.commitAuditTrail();
				approvor.registerApproval(registry, auditor);
			} else {
				logger.debug("No approval role for service {}. AutoApproving {}", service.getName(), user.getEppn());
				auditor.logAction(user.getEppn(), "STARTING AUTO APPROVE", "registry-" + registry.getId(),
						"Autoapproving registry", AuditStatus.SUCCESS);
				auditor.finishAuditTrail();
				if (parentAuditor == null)
					auditor.commitAuditTrail();
				approvor.approve(registry, executor, sendGroupUpdate, auditor);
			}

			return registry;
		} catch (Throwable t) {
			throw new RegisterException(t);
		}
	}

	@RetryTransaction
	public Boolean processUpdateGroup(ServiceGroupFlagEntity flag, Boolean reconRegistries, Boolean fullRecon,
			String executor) {
		flag = groupFlagDao.fetch(flag.getId());
		ServiceEntity service = flag.getService();
		ServiceBasedGroupEntity group = flag.getGroup();

		if (flag.getStatus().equals(ServiceGroupStatus.CLEAN)) {
			logger.debug("Skipping groupFlag {} fro group {} and service {}. It is already marked clean.", flag.getId(),
					group.getName(), service.getName());
			return false;
		}

		logger.debug("Processing groupFlag {} for group {} and service {}", flag.getId(), group.getName(),
				service.getName());

		long a = 0L;

		RegisterUserWorkflow workflow = getWorkflowInstance(service.getRegisterBean());
		if (!(workflow instanceof GroupCapable)) {
			logger.warn("Workflow " + workflow.getClass() + " is not GroupCapable! Resetting flags to CLEAN");
			flag.setStatus(ServiceGroupStatus.CLEAN);
			return false;
		}

		if (ServiceGroupStatus.TO_DELETE.equals(flag.getStatus())) {
			logger.info("Group {} at Service {} is about to get deleted", group.getName(), service.getName());
			try {
				deleteGroup(group, service, executor);
				groupFlagDao.delete(flag);
				return true;
			} catch (RegisterException e) {
				logger.warn("Could not delete group: " + e);
				return false;
			}
		} else if (ServiceGroupStatus.DIRTY.equals(flag.getStatus())) {
			logger.info("Group {} at Service {} needs an update", group.getName(), flag.getService().getName());

			// check parent groups (These contain the change groups and their members)
//			for (GroupEntity parent : serviceBasedGroup.getParents()) {
//				updateParentGroup(parent, groupPerServiceList, 0, 3);
//			}

			// service has a group filter defined. We need to check, if this group has to be
			// processed
			Boolean retainGroup = true;
			if (service.getGroupFilterRulePackage() != null) {
				BusinessRulePackageEntity rulePackage = service.getGroupFilterRulePackage();
				KieSession ksession = knowledgeSessionService.getStatefulSession(rulePackage.getPackageName(),
						rulePackage.getKnowledgeBaseName(), rulePackage.getKnowledgeBaseVersion());

				ksession.setGlobal("logger", logger);
				ksession.insert(group);

				ksession.fireAllRules();
				List<Object> objectList = new ArrayList<Object>(ksession.getObjects());

				retainGroup = false;
				for (Object o : objectList) {
					ksession.delete(ksession.getFactHandle(o));
					if (o.equals(group))
						retainGroup = true;
				}
			}

			GroupUpdateStructure updateStruct = new GroupUpdateStructure();

			if (retainGroup) {
				// keep group
				a = System.currentTimeMillis();
				Set<UserEntity> users = groupUtil.rollUsersForGroup(group, service);
				logger.debug("RollGroup {} took {} ms", group.getName(), (System.currentTimeMillis() - a));
				a = System.currentTimeMillis();

				updateStruct.addGroup(flag, users);
			} else {
				// group is filtered. Just set no members.
				updateStruct.addGroup(flag, new HashSet<UserEntity>());
			}

			NullAuditor auditor = new NullAuditor();

			try {
				((GroupCapable) workflow).updateGroups(service, updateStruct, auditor);
				// succesful operation, clean flag
				flag.setStatus(ServiceGroupStatus.CLEAN);
				return true;
			} catch (RegisterException e) {
				if (e.getCause() != null)
					logger.info("RegisterException {} happened, cause is {}", e.getMessage(),
							e.getCause().getMessage());
				else
					logger.info("RegisterException {} happened, no cause", e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	@RetryTransaction
	public GroupPerServiceList buildGroupPerServiceList(Set<GroupEntity> groupUpdateSet, Boolean reconRegistries,
			Boolean fullRecon, String executor) {
		GroupPerServiceList groupPerServiceList = new GroupPerServiceList();

		for (GroupEntity group : groupUpdateSet) {

			logger.debug("Analyzing group {} of type {}", group.getName(), group.getClass().getSimpleName());

			if (group instanceof ServiceBasedGroupEntity) {

				ServiceBasedGroupEntity serviceBasedGroup = (ServiceBasedGroupEntity) groupDao.fetch(group.getId());

				for (ServiceGroupFlagEntity flag : serviceBasedGroup.getServiceGroupFlags()) {
					if (ServiceGroupStatus.DIRTY.equals(flag.getStatus())) {
						logger.info("Group {} at Service {} needs an update", serviceBasedGroup.getName(),
								flag.getService().getName());
						groupPerServiceList.addGroupToUpdate(flag);

						// check parent groups (These contain the change groups and their members)
						for (GroupEntity parent : serviceBasedGroup.getParents()) {
							updateParentGroup(parent, groupPerServiceList, 0, 3);
						}
					} else if (ServiceGroupStatus.TO_DELETE.equals(flag.getStatus())) {
						logger.info("Group {} at Service {} is about to get deleted", serviceBasedGroup.getName(),
								flag.getService().getName());
						try {
							deleteGroup(serviceBasedGroup, flag.getService(), executor);
							groupFlagDao.delete(flag);
							// also recon registries here
							if (reconRegistries) {
								if (!(serviceBasedGroup instanceof HomeOrgGroupEntity)) {
									for (UserGroupEntity user : serviceBasedGroup.getUsers()) {
										RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(
												flag.getService(), user.getUser(), RegistryStatus.ACTIVE);
										if (registry != null) {
											reconsiliation(registry, fullRecon, executor);
										}
									}
								}
							}
						} catch (RegisterException e) {
							logger.warn("Could not delete group: " + e);
						}
					}

				}

			} else {
				logger.debug("Group {} is no ServiceBasedGroup. Doin' nuthin at all for now.");
			}
		}

		return groupPerServiceList;
	}

	protected void updateParentGroup(GroupEntity group, GroupPerServiceList groupUpdateList, int depth, int maxDepth) {
		if (depth <= maxDepth) {
			if (group instanceof ServiceBasedGroupEntity) {
				ServiceBasedGroupEntity serviceBasedGroup = (ServiceBasedGroupEntity) groupDao.fetch(group.getId());

				for (ServiceGroupFlagEntity flag : serviceBasedGroup.getServiceGroupFlags()) {
					logger.info("Parentgroup {} at Service {} needs an update", serviceBasedGroup.getName(),
							flag.getService().getName());
					groupUpdateList.addGroupToUpdate(flag);

					for (GroupEntity parent : serviceBasedGroup.getParents()) {
						updateParentGroup(parent, groupUpdateList, 0, 3);
					}
				}
			}
		}
	}

	public void deleteGroup(GroupEntity group, ServiceEntity service, String executor) throws RegisterException {
		group = groupDao.find(equal(GroupEntity_.id, group.getId()), GroupEntity_.users);

		RegisterUserWorkflow workflow = getWorkflowInstance(service.getRegisterBean());

		if (!(workflow instanceof GroupCapable)) {
			logger.warn("Workflow " + workflow.getClass() + " is not GroupCapable! But Group will be deleted anyway.");
			return;
		}

		try {
			GroupAuditor auditor = new GroupAuditor(auditDao, auditDetailDao, appConfig);
			auditor.startAuditTrail(executor);
			auditor.setName(workflow.getClass().getName() + "-GroupDelete-Audit");
			auditor.setDetail("Delete group " + group.getName() + " (" + group.getGidNumber() + ") for service "
					+ service.getName());
			auditor.setGroup(group);

			((GroupCapable) workflow).deleteGroup((ServiceBasedGroupEntity) group, service, auditor);

			auditor.finishAuditTrail();
			auditor.commitAuditTrail();
		} catch (Throwable t) {
			throw new RegisterException(t);
		}
	}

	public void reconsiliation(RegistryEntity registry, Boolean fullRecon, String executor) throws RegisterException {
		reconsiliation(registry, fullRecon, executor, null);
	}

	public void reconsiliation(RegistryEntity registry, Boolean fullRecon, String executor, Auditor parentAuditor)
			throws RegisterException {

		RegisterUserWorkflow workflow = getWorkflowInstance(registry.getRegisterBean());

		try {
			registry = registryDao.fetch(registry.getId());
			ServiceEntity serviceEntity = registry.getService();
			UserEntity userEntity = registry.getUser();

			RegistryAuditor auditor = new RegistryAuditor(auditDao, auditDetailDao, appConfig);
			auditor.startAuditTrail(executor);
			auditor.setName(workflow.getClass().getName() + "-Reconsiliation-Audit");
			auditor.setDetail("Recon user " + userEntity.getEppn() + " for service " + serviceEntity.getName());
			auditor.setParent(parentAuditor);
			auditor.setRegistry(registry);

			Boolean missingMandatoryValues = false;

			if (serviceEntity.getMandatoryValueRulePackage() != null) {

				BusinessRulePackageEntity rulePackage = registry.getService().getMandatoryValueRulePackage();
				KieSession ksession = knowledgeSessionService.getStatefulSession(rulePackage.getPackageName(),
						rulePackage.getKnowledgeBaseName(), rulePackage.getKnowledgeBaseVersion());

				ksession.setGlobal("logger", logger);
				ksession.insert(registry);
				ksession.insert(userEntity);
				ksession.insert(serviceEntity);

				ksession.fireAllRules();
				List<Object> objectList = new ArrayList<Object>(ksession.getObjects());

				for (Object o : objectList) {
					ksession.delete(ksession.getFactHandle(o));
					if (o instanceof MissingMandatoryValues)
						missingMandatoryValues = true;
				}
			}

			if (missingMandatoryValues) {
				logger.info("Missing mandatory values! Skipping update (user: {}, service: {}, registry: {})",
						userEntity.getEppn(), serviceEntity.getName(), registry.getId());
				if (RegistryStatus.ACTIVE.equals(registry.getRegistryStatus())) {
					registry.setRegistryStatus(RegistryStatus.INVALID);
					registry.setStatusMessage("missing-mandatory-values");
					registry.setLastStatusChange(new Date());
				}
			} else {
				Boolean updated = workflow.updateRegistry(userEntity, serviceEntity, registry, auditor);

				if (RegistryStatus.INVALID.equals(registry.getRegistryStatus())) {
					registry.setRegistryStatus(RegistryStatus.ACTIVE);
					registry.setStatusMessage(null);
					registry.setLastStatusChange(new Date());
				}

				if (fullRecon) {
					logger.debug("Doing full reconsiliation (user: {}, service: {}, registry: {})",
							userEntity.getEppn(), serviceEntity.getName(), registry.getId());
					workflow.reconciliation(userEntity, serviceEntity, registry, auditor);
				} else if (updated) {
					logger.debug("Changes detected, starting reconcile (user: {}, service: {}, registry: {})",
							userEntity.getEppn(), serviceEntity.getName(), registry.getId());
					workflow.reconciliation(userEntity, serviceEntity, registry, auditor);
				} else {
					logger.debug("No Changes detected (user: {}, service: {}, registry: {})", userEntity.getEppn(),
							serviceEntity.getName(), registry.getId());
				}
			}

			registry.setLastReconcile(new Date());
			registry = registryDao.persist(registry);

			auditor.finishAuditTrail();
			if (parentAuditor == null)
				auditor.commitAuditTrail();

		} catch (Throwable t) {
			throw new RegisterException(t);
		}
	}

	public void reconsiliationByUser(UserEntity user, Boolean fullRecon, String executor) {
		List<RegistryEntity> registryList = registryDao.findByIdentityAndStatus(user.getIdentity(),
				RegistryStatus.ACTIVE);
		for (RegistryEntity registry : registryList) {
			try {
				reconsiliation(registry, fullRecon, executor, null);
			} catch (RegisterException e) {
				logger.warn("Could not recon registry {} for user {}: {}", registry.getId(), user.getId(), e);
			}
		}
	}

	public void deregisterUser(RegistryEntity registry, String executor, String statusMessage)
			throws RegisterException {
		deregisterUser(registry, executor, null, statusMessage);
	}

	public void deregisterUser(RegistryEntity registry, String executor, Auditor parentAuditor, String statusMessage)
			throws RegisterException {

		registry = registryDao.fetch(registry.getId());

		if (RegistryStatus.DELETED.equals(registry.getRegistryStatus())) {
			throw new RegisterException("Registry " + registry.getId() + " is already deregistered!");
		}

		RegisterUserWorkflow workflow = getWorkflowInstance(registry.getRegisterBean());

		try {
			ServiceEntity serviceEntity = registry.getService();
			UserEntity userEntity = registry.getUser();

			ServiceRegisterAuditor auditor = new ServiceRegisterAuditor(auditDao, auditDetailDao, appConfig);
			auditor.startAuditTrail(executor);
			auditor.setName(workflow.getClass().getName() + "-Deregister-Audit");
			auditor.setDetail(
					"Deregister user " + registry.getUser().getEppn() + " for service " + serviceEntity.getName());
			auditor.setParent(parentAuditor);
			auditor.setRegistry(registry);

			workflow.deregisterUser(userEntity, serviceEntity, registry, auditor);

			registry.setRegistryStatus(RegistryStatus.DELETED);
			registry.setStatusMessage(null);
			registry.setLastStatusChange(new Date());
			registry = registryDao.persist(registry);

			if (userEntity.getGroups() != null) {
				HashSet<GroupEntity> userGroups = new HashSet<GroupEntity>(userEntity.getGroups().size());

				for (UserGroupEntity userGroup : userEntity.getGroups()) {
					GroupEntity group = userGroup.getGroup();
					userGroups.add(group);

					if (group instanceof ServiceBasedGroupEntity) {
						List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao
								.findByGroupAndService((ServiceBasedGroupEntity) group, serviceEntity);
						for (ServiceGroupFlagEntity groupFlag : groupFlagList) {
							groupFlag.setStatus(ServiceGroupStatus.DIRTY);
							groupFlag = groupFlagDao.persist(groupFlag);
						}
					}
				}

				MultipleGroupEvent mge = new MultipleGroupEvent(userGroups);
				try {
					eventSubmitter.submit(mge, EventType.GROUP_UPDATE, auditor.getActualExecutor());
				} catch (EventSubmitException e) {
					logger.warn("Exeption", e);
				}
			}

			ServiceRegisterEvent serviceRegisterEvent = new ServiceRegisterEvent(registry);
			List<EventEntity> eventList = new ArrayList<EventEntity>(serviceEventDao.findAllByService(serviceEntity));
			eventSubmitter.submit(serviceRegisterEvent, eventList, EventType.SERVICE_DEREGISTER, executor);

			List<SshPubKeyRegistryEntity> pubKeyRegistryList = sshPubKeyRegistryDao
					.findAll(equal("registry.id", registry.getId()));

			for (SshPubKeyRegistryEntity sshPubKeyRegistryEntity : pubKeyRegistryList) {
				sshPubKeyRegistryDao.delete(sshPubKeyRegistryEntity);

				SshPubKeyRegistryEvent event = new SshPubKeyRegistryEvent(sshPubKeyRegistryEntity);
				try {
					eventSubmitter.submit(event, EventType.SSH_KEY_REGISTRY_DELETED, executor);
				} catch (EventSubmitException e) {
					logger.warn("Could not submit event", e);
				}
			}

			if (registry.getService().getParentService() != null) {
				/*
				 * find all active registries for parent service
				 */
				List<ServiceEntity> sisterServiceList = serviceDao
						.findByParentService(registry.getService().getParentService());

				Boolean activeSisterRegistry = false;

				for (ServiceEntity sisterService : sisterServiceList) {
					List<RegistryEntity> sisterRegistryList = registryDao.findByServiceAndIdentityAndNotStatus(
							sisterService, userEntity.getIdentity(), RegistryStatus.DELETED,
							RegistryStatus.DEPROVISIONED);

					if (sisterRegistryList.size() > 0) {
						activeSisterRegistry = true;
						break;
					}
				}

				if (!activeSisterRegistry) {
					/*
					 * no more registries for parent service left. Deregister parent.
					 */
					List<RegistryEntity> parentRegistryList = registryDao.findByServiceAndIdentityAndNotStatus(
							registry.getService().getParentService(), userEntity.getIdentity(), RegistryStatus.DELETED,
							RegistryStatus.DEPROVISIONED);

					for (RegistryEntity parentRegistry : parentRegistryList) {
						deregisterUser(parentRegistry, executor, parentAuditor, "all-child-deregistered");
					}
				}
			}

			auditor.finishAuditTrail();
			if (parentAuditor == null) {
				auditor.commitAuditTrail();
			}

		} catch (RegisterException e) {
			throw e;
		} catch (Throwable t) {
			throw new RegisterException(t);
		}
	}

	public void setPassword(UserEntity user, ServiceEntity service, RegistryEntity registry, String password,
			String executor) throws RegisterException {

		RegisterUserWorkflow workflow = getWorkflowInstance(registry.getRegisterBean());

		try {
			ServiceEntity serviceEntity = registry.getService();
			UserEntity userEntity = registry.getUser();

			String passwordRegex;
			if (serviceEntity.getServiceProps().containsKey("password_regex"))
				passwordRegex = serviceEntity.getServiceProps().get("password_regex");
			else
				passwordRegex = ".{6,}";

			String passwordRegexMessage;
			if (serviceEntity.getServiceProps().containsKey("password_regex_message"))
				passwordRegexMessage = serviceEntity.getServiceProps().get("password_regex_message");
			else
				passwordRegexMessage = "Das Passwort ist nicht komplex genug";

			if (!password.matches(passwordRegex))
				throw new RegisterException(passwordRegexMessage);

			ServiceRegisterAuditor auditor = new ServiceRegisterAuditor(auditDao, auditDetailDao, appConfig);
			auditor.startAuditTrail(executor);
			auditor.setName(workflow.getClass().getName() + "-SetPassword-Audit");
			auditor.setDetail("Setting service password for user " + registry.getUser().getEppn() + " for service "
					+ serviceEntity.getName());
			auditor.setRegistry(registry);

			if (serviceEntity.getServiceProps().containsKey("pw_location")
					&& serviceEntity.getServiceProps().get("pw_location").equalsIgnoreCase("registry")) {
				registry.getRegistryValues().put("userPassword", passwordUtil.generatePassword("SHA-512", password));
			} else if (serviceEntity.getServiceProps().containsKey("pw_location")
					&& serviceEntity.getServiceProps().get("pw_location").equalsIgnoreCase("both")) {
				registry.getRegistryValues().put("userPassword", passwordUtil.generatePassword("SHA-512", password));
				((SetPasswordCapable) workflow).setPassword(userEntity, serviceEntity, registry, auditor, password);
			} else {
				((SetPasswordCapable) workflow).setPassword(userEntity, serviceEntity, registry, auditor, password);
			}

			registry = registryDao.persist(registry);

			auditor.finishAuditTrail();
			auditor.commitAuditTrail();

			ServiceRegisterEvent serviceRegisterEvent = new ServiceRegisterEvent(registry, auditor.getAudit());
			eventSubmitter.submit(serviceRegisterEvent, EventType.REGISTRY_PASSWORD_CHANGE, executor);
		} catch (RegisterException e) {
			throw e;
		} catch (Throwable t) {
			throw new RegisterException(t);
		}
	}

	public void deletePassword(UserEntity user, ServiceEntity service, RegistryEntity registry, String executor)
			throws RegisterException {

		RegisterUserWorkflow workflow = getWorkflowInstance(registry.getRegisterBean());

		try {
			ServiceEntity serviceEntity = registry.getService();
			UserEntity userEntity = registry.getUser();

			ServiceRegisterAuditor auditor = new ServiceRegisterAuditor(auditDao, auditDetailDao, appConfig);
			auditor.startAuditTrail(executor);
			auditor.setName(workflow.getClass().getName() + "-DeletePassword-Audit");
			auditor.setDetail("Delete service password for user " + registry.getUser().getEppn() + " for service "
					+ serviceEntity.getName());
			auditor.setRegistry(registry);

			registry.getRegistryValues().remove("userPassword");
			((SetPasswordCapable) workflow).deletePassword(userEntity, serviceEntity, registry, auditor);

			registry = registryDao.persist(registry);

			auditor.finishAuditTrail();
			auditor.commitAuditTrail();
		} catch (RegisterException e) {
			throw e;
		} catch (Throwable t) {
			throw new RegisterException(t);
		}
	}

	public Boolean checkWorkflow(String name) {
		if (getWorkflowInstance(name) != null) {
			return true;
		} else {
			return false;
		}
	}

	public RegisterUserWorkflow getWorkflowInstance(String className) {
		try {
			Object o = Class.forName(className).getConstructor().newInstance();
			if (o instanceof RegisterUserWorkflow) {
				if (o instanceof ScriptingWorkflow)
					((ScriptingWorkflow) o).setScriptingEnv(scriptingEnv);

				return (RegisterUserWorkflow) o;
			} else {
				logger.warn("Service Register bean misconfigured, Object not Type RegisterUserWorkflow but: {}",
						o.getClass());
				return null;
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException
				| InvocationTargetException e) {
			logger.warn("Service Register bean misconfigured: {}", e.getMessage());
			return null;
		}
	}

	public void reconGroupsForRegistry(RegistryEntity registry, String executor) {
		ServiceEntity serviceEntity = registry.getService();
		UserEntity userEntity = registry.getUser();

		HashSet<GroupEntity> userGroups = new HashSet<GroupEntity>(userEntity.getGroups().size());

		for (UserGroupEntity userGroup : userEntity.getGroups()) {
			GroupEntity group = userGroup.getGroup();
			userGroups.add(group);

			if (group instanceof ServiceBasedGroupEntity) {
				List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao
						.findByGroupAndService((ServiceBasedGroupEntity) group, serviceEntity);
				for (ServiceGroupFlagEntity groupFlag : groupFlagList) {
					groupFlag.setStatus(ServiceGroupStatus.DIRTY);
					groupFlag = groupFlagDao.persist(groupFlag);
				}
			}
		}

		MultipleGroupEvent mge = new MultipleGroupEvent(userGroups);
		try {
			eventSubmitter.submit(mge, EventType.GROUP_UPDATE, executor);
		} catch (EventSubmitException e) {
			logger.warn("Exeption", e);
		}
	}

	public void completeReconciliation(ServiceEntity service, Boolean fullRecon, Boolean withGroups, Boolean onlyActive,
			String executor) {

		List<RegistryEntity> registryList;

		if (onlyActive) {
			registryList = registryDao.findByServiceAndStatus(service, RegistryStatus.ACTIVE);
		} else {
			registryList = registryDao.findByService(service);
		}

		logger.info("Found {} registries for service {}", registryList.size(), service.getName());

		for (RegistryEntity registry : registryList) {
			logger.info("Recon registry {}", registry.getId());
			try {
				reconsiliation(registry, fullRecon, executor);
			} catch (RegisterException e) {
				logger.warn("Could not recon registry {}: {}", registry.getId(), e);
			}
		}

		if (withGroups && service.getGroupCapable()) {
			HashSet<GroupEntity> groups = new HashSet<GroupEntity>();
			List<HashSet<GroupEntity>> chunkList = new ArrayList<>();

			List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao.findByService(service);

			logger.info("Setting groupFlags to dirty");
			int i = 0;

			for (ServiceGroupFlagEntity groupFlag : groupFlagList) {
				if ((i % 50) == 0) {
					groups = new HashSet<GroupEntity>();
					chunkList.add(groups);
				}
				groupFlag.setStatus(ServiceGroupStatus.DIRTY);
				groupFlag = groupFlagDao.persist(groupFlag);
				groups.add(groupFlag.getGroup());
				i++;
			}

			logger.info("Sending Group Update Events");

			for (HashSet<GroupEntity> innerGroup : chunkList) {
				MultipleGroupEvent mge = new MultipleGroupEvent(innerGroup);
				try {
					eventSubmitter.submit(mge, EventType.GROUP_UPDATE, executor);
				} catch (EventSubmitException e) {
					logger.warn("Exeption", e);
				}
			}
		}

		logger.info("Reconciliation is done.");
	}

	public void completeReconciliationForRegistry(ServiceEntity service, RegistryEntity registry, Boolean fullRecon,
			Boolean withGroups, String executor) throws RegisterException {

		logger.info("Recon registry {} with fullRecon={} and withGroups={}", registry.getId(), fullRecon, withGroups);
		try {
			reconsiliation(registry, fullRecon, executor);
		} catch (RegisterException e) {
			logger.warn("Could not recon registry {}: {}", registry.getId(), e);
			throw e;
		}

		if (withGroups && service.getGroupCapable()) {
			HashSet<GroupEntity> groups = new HashSet<GroupEntity>();
			List<HashSet<GroupEntity>> chunkList = new ArrayList<>();

			List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao.findByService(service);

			logger.info("Setting groupFlags to dirty");
			int i = 0;

			for (ServiceGroupFlagEntity groupFlag : groupFlagList) {
				if ((i % 50) == 0) {
					groups = new HashSet<GroupEntity>();
					chunkList.add(groups);
				}
				groupFlag.setStatus(ServiceGroupStatus.DIRTY);
				groupFlag = groupFlagDao.persist(groupFlag);
				groups.add(groupFlag.getGroup());
				i++;
			}

			logger.info("Sending Group Update Events");

			for (HashSet<GroupEntity> innerGroup : chunkList) {
				MultipleGroupEvent mge = new MultipleGroupEvent(innerGroup);
				try {
					eventSubmitter.submit(mge, EventType.GROUP_UPDATE, executor);
				} catch (EventSubmitException e) {
					logger.warn("Exeption", e);
					throw new RegisterException("could not recon registry for groups: " + e.getMessage());
				}
			}
		}

		logger.info("Reconciliation is done.");
	}

	public void deprovision(RegistryEntity registry, String executor) throws RegisterException {

		if (!RegistryStatus.DELETED.equals(registry.getRegistryStatus())) {
			throw new RegisterException("only registry with status deleted can be deprovisioned");
		}

		ServiceRegisterAuditor auditor = new ServiceRegisterAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(registry.getService().getShortName() + "-Deregister-Audit");
		auditor.setDetail("Deprovision user " + registry.getUser().getEppn() + " for service "
				+ registry.getService().getShortName());
		auditor.setRegistry(registry);

		registry.setRegistryStatus(RegistryStatus.DEPROVISIONED);
		registry.setStatusMessage(null);
		registry.setLastStatusChange(new Date());
		registry = registryDao.persist(registry);

		auditor.finishAuditTrail();
		auditor.commitAuditTrail();
	}

	public void purge(RegistryEntity registry, String executor) throws RegisterException {
		if (RegistryStatus.ACTIVE.equals(registry.getRegistryStatus())
				|| RegistryStatus.INVALID.equals(registry.getRegistryStatus())
				|| RegistryStatus.LOST_ACCESS.equals(registry.getRegistryStatus())
				|| RegistryStatus.BLOCKED.equals(registry.getRegistryStatus())) {

			/*
			 * Deregister user first, if the registration is somewhat active
			 */
			deregisterUser(registry, executor, "purged");
		}

		List<AuditServiceRegisterEntity> auditList = auditDao.findAllServiceRegister(registry);

		logger.info("There are {} AuditServiceRegisterEntity for Registry {} to be deleted", auditList.size(),
				registry.getId());

		for (AuditServiceRegisterEntity audit : auditList) {
			logger.debug("Deleting audit {} with {} auditentries", audit.getId(), audit.getAuditDetails().size());
			for (AuditDetailEntity detail : audit.getAuditDetails()) {
				auditDetailDao.delete(detail);
			}
			auditDao.delete(audit);
		}

		registryDao.delete(registry);

	}
}
