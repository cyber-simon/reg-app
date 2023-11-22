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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.ApprovalAuditor;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceEventDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.EventEntity;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.event.ServiceRegisterEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.reg.ApprovalWorkflow;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.ScriptingWorkflow;

@ApplicationScoped
public class Approvor implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private ServiceGroupFlagDao groupFlagDao;
	
	@Inject
	private ServiceEventDao serviceEventDao;
	
	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private ScriptingEnv scriptingEnv;
	
	public void registerApproval(RegistryEntity registry, Auditor parentAuditor) throws RegisterException {
		ApprovalWorkflow workflow = getApprovalWorkflowInstance(registry.getApprovalBean());
		workflow.startWorkflow(registry);
		
		ServiceRegisterEvent serviceRegisterEvent = new ServiceRegisterEvent(registry);
		List<EventEntity> eventList = new ArrayList<EventEntity>(serviceEventDao.findAllByService(registry.getService()));
		try {
			eventSubmitter.submit(serviceRegisterEvent, eventList, EventType.APPROVAL_START, "user-self");
		} catch (EventSubmitException e) {
			logger.warn("Exeption", e);
		}		
	}

	public void denyApproval(RegistryEntity registry, String executor, Auditor parentAuditor) throws RegisterException {
		ApprovalAuditor auditor = new ApprovalAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor, true);
		auditor.setName(this.getClass().getName() + "-ApprovalDeny-Audit");
		auditor.setDetail("Deny user " + registry.getUser().getEppn() + " for service " + registry.getService().getName());
		auditor.setParent(parentAuditor);
		auditor.setRegistry(registry);

		registry.setRegistryStatus(RegistryStatus.DELETED);
		registry.setStatusMessage("approval-denied");
		auditor.logAction(registry.getUser().getEppn(), "DENY APPROVAL", "registry-" + registry.getId(), "User is denied acces for service", AuditStatus.SUCCESS);
		
		ServiceRegisterEvent serviceRegisterEvent = new ServiceRegisterEvent(registry, auditor.getAudit());
		List<EventEntity> eventList = new ArrayList<EventEntity>(serviceEventDao.findAllByService(registry.getService()));
		try {
			eventSubmitter.submit(serviceRegisterEvent, eventList, EventType.APPROVAL_DENIED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Exeption", e);
		}	
		
		auditor.finishAuditTrail();
		if (parentAuditor == null)
			auditor.commitAuditTrail();
	}

	public void approve(RegistryEntity registry, String executor, Auditor parentAuditor)
			throws RegisterException {
		approve(registry, executor, true, parentAuditor);
	}
	
	public void approve(RegistryEntity registry, String executor, Boolean sendGroupUpdate, Auditor parentAuditor)
			throws RegisterException {
		logger.info("Finally approving registry {} for user {} and service {}", registry.getId(),
				registry.getUser().getEppn(), registry.getService().getName());

		RegisterUserWorkflow workflow = getRegisterWorkflowInstance(registry.getRegisterBean());
		
		try {

			ServiceEntity serviceEntity = registry.getService();
			UserEntity userEntity = registry.getUser();

			ApprovalAuditor auditor = new ApprovalAuditor(auditDao, auditDetailDao, appConfig);
			auditor.startAuditTrail(executor, true);
			auditor.setName(workflow.getClass().getName() + "-ApprovalApprove-Audit");
			auditor.setDetail("Approve user " + userEntity.getEppn() + " for service " + serviceEntity.getName());
			auditor.setParent(parentAuditor);
			auditor.setRegistry(registry);
			
			workflow.updateRegistry(userEntity, serviceEntity, registry, auditor);
			workflow.registerUser(userEntity, serviceEntity, registry, auditor);

			registry.setRegistryStatus(RegistryStatus.ACTIVE);
			registry.setStatusMessage(null);
			registry.setLastStatusChange(new Date());
			registry.setLastReconcile(new Date());

			auditor.logAction(registry.getUser().getEppn(), "APPROVE", "registry-" + registry.getId(), "User is approved for service", AuditStatus.SUCCESS);

			HashSet<GroupEntity> userGroups = new HashSet<GroupEntity>(userEntity.getGroups().size());
			
			for (UserGroupEntity userGroup : userEntity.getGroups()) {
				GroupEntity group = userGroup.getGroup();
				userGroups.add(group);
				
				if (group instanceof ServiceBasedGroupEntity) {
					List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao.findByGroupAndService((ServiceBasedGroupEntity) group, serviceEntity);
					for (ServiceGroupFlagEntity groupFlag : groupFlagList) {
						groupFlag.setStatus(ServiceGroupStatus.DIRTY);
						groupFlag = groupFlagDao.persist(groupFlag);
					}
				}
			}
			
			if (sendGroupUpdate) {
				MultipleGroupEvent mge = new MultipleGroupEvent(userGroups);
				try {
					eventSubmitter.submit(mge, EventType.GROUP_UPDATE, auditor.getActualExecutor());
				} catch (EventSubmitException e) {
					logger.warn("Exeption", e);
				}
			}
			
			ServiceRegisterEvent serviceRegisterEvent = new ServiceRegisterEvent(registry, auditor.getAudit());
			List<EventEntity> eventList = new ArrayList<EventEntity>(serviceEventDao.findAllByService(serviceEntity));
			eventSubmitter.submit(serviceRegisterEvent, eventList, EventType.SERVICE_REGISTER, executor);

			auditor.finishAuditTrail();
			if (parentAuditor == null)
				auditor.commitAuditTrail();
			
		} catch (Throwable t) {
			throw new RegisterException(t);
		}    	

	}

	private RegisterUserWorkflow getRegisterWorkflowInstance(String className) {
		try {
			Object o = Class.forName(className).getConstructor().newInstance();
			if (o instanceof RegisterUserWorkflow) {
				if (o instanceof ScriptingWorkflow)
					((ScriptingWorkflow) o).setScriptingEnv(scriptingEnv);
				
				return (RegisterUserWorkflow) o;
			}
			else {
				logger.warn("Service Register bean misconfigured, Object not Type RegisterUserWorkflow but: {}", o.getClass());
				return null;
			}
		} catch (InstantiationException | NoSuchMethodException | IllegalAccessException | 
				ClassNotFoundException | InvocationTargetException e) {
			logger.warn("Service Register bean misconfigured: {}", e.getMessage());
			return null;
		}
	}

	private ApprovalWorkflow getApprovalWorkflowInstance(String className) {
		try {
			Object o = Class.forName(className).getConstructor().newInstance();
			if (o instanceof ApprovalWorkflow)
				return (ApprovalWorkflow) o;
			else {
				logger.warn("Service Register bean misconfigured, Object not Type ApprovalWorkflow but: {}", o.getClass());
				return null;
			}
		} catch (InstantiationException | NoSuchMethodException | IllegalAccessException | 
				ClassNotFoundException | InvocationTargetException e) {
			logger.warn("Service Register bean misconfigured: {}", e.getMessage());
			return null;
		}
	}
	
}
