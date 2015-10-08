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
package edu.kit.scc.webreg.bean;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.approval.ApproverRoleApprovalWorkflow;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.MisconfiguredServiceException;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class ApproveUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(ApproveUserBean.class);
	
	@Inject
	private RegistryService service;
	
	@Inject
	private SessionManager sessionManager;

    @Inject
    private AuthorizationBean authBean;
	
	private RegistryEntity entity;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findById(id);
			
			if (entity == null) {
				throw new NotAuthorizedException("Nicht autorisiert");
			}

			if (! authBean.isUserServiceApprover(entity.getService()))
				throw new NotAuthorizedException("Nicht autorisiert");
		}
	}
	
	public String approve() {
		ApproverRoleApprovalWorkflow approvalWorkflow = getApprovalWorkflowInstance(entity.getApprovalBean());
		
		if (approvalWorkflow == null)
			throw new MisconfiguredServiceException("Der Approval Prozess für den Dienst ist nicht korrekt konfiguriert");
		
		try {
			approvalWorkflow.approveRegistry(entity, "user-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.warn("Could not approve user registry", e);
			return "";
		}
		entity = service.save(entity);
		
		return ViewIds.LIST_APPROVALS + "?faces-redirect=true&serviceId=" + entity.getService().getId();
	}

	public String deny() {
		ApproverRoleApprovalWorkflow approvalWorkflow = getApprovalWorkflowInstance(entity.getApprovalBean());
		
		if (approvalWorkflow == null)
			throw new MisconfiguredServiceException("Der Approval Prozess für den Dienst ist nicht korrekt konfiguriert");
		
		try {
			approvalWorkflow.denyApproval(entity, "user-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.warn("Could not deny approval for user registry", e);
			return "";
		}
		entity = service.save(entity);
		
		return ViewIds.LIST_APPROVALS + "?faces-redirect=true&serviceId=" + entity.getService().getId();
	}

	public String cancel() {
		return ViewIds.LIST_APPROVALS + "?faces-redirect=true&serviceId=" + entity.getService().getId();
	}
	
	public RegistryEntity getEntity() {
		return entity;
	}

	public void setEntity(RegistryEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	private ApproverRoleApprovalWorkflow getApprovalWorkflowInstance(String className) {
		try {
			Object o = Class.forName(className).newInstance();
			if (o instanceof ApproverRoleApprovalWorkflow)
				return (ApproverRoleApprovalWorkflow) o;
			else {
				logger.warn("Service Register bean misconfigured, Object not Type ApprovalWorkflow but: {}", o.getClass());
				return null;
			}
		} catch (InstantiationException e) {
			logger.warn("Service Register bean misconfigured: {}", e.getMessage());
			return null;
		} catch (IllegalAccessException e) {
			logger.warn("Service Register bean misconfigured: {}", e.getMessage());
			return null;
		} catch (ClassNotFoundException e) {
			logger.warn("Service Register bean misconfigured: {}", e.getMessage());
			return null;
		}
	}	
}
