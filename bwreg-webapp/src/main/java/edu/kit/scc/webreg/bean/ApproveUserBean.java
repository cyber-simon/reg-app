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
import java.lang.reflect.InvocationTargetException;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.approval.ApproverRoleApprovalWorkflow;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity_;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.MisconfiguredServiceException;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class ApproveUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(ApproveUserBean.class);
	
	@Inject
	private RegistryService service;
	
	@Inject
	private UserService userService;
	
	@Inject
	private SessionManager sessionManager;

    @Inject
    private AuthorizationBean authBean;
	
	private RegistryEntity entity;
	private UserEntity user;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.fetch(id);
			if (entity.getUser() instanceof SamlUserEntity)
				user = userService.findByIdWithAttrs(entity.getUser().getId(), SamlUserEntity_.idp);
			else
				user = entity.getUser();
			
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
			approvalWorkflow.approveRegistry(entity, "identity-" + sessionManager.getIdentityId());
		} catch (RegisterException e) {
			logger.warn("Could not approve user registry", e);
			return "";
		}
		
		return ViewIds.LIST_APPROVALS + "?faces-redirect=true&serviceId=" + entity.getService().getId();
	}

	public String deny() {
		ApproverRoleApprovalWorkflow approvalWorkflow = getApprovalWorkflowInstance(entity.getApprovalBean());
		
		if (approvalWorkflow == null)
			throw new MisconfiguredServiceException("Der Approval Prozess für den Dienst ist nicht korrekt konfiguriert");
		
		try {
			approvalWorkflow.denyApproval(entity, "identity-" + sessionManager.getIdentityId());
		} catch (RegisterException e) {
			logger.warn("Could not deny approval for user registry", e);
			return "";
		}
		
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
			Object o = Class.forName(className).getConstructor().newInstance();
			if (o instanceof ApproverRoleApprovalWorkflow)
				return (ApproverRoleApprovalWorkflow) o;
			else {
				logger.warn("Service Register bean misconfigured, Object not Type ApprovalWorkflow but: {}", o.getClass());
				return null;
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
			logger.warn("Service Register bean misconfigured: {}", e.getMessage());
			return null;
		}
	}

	public UserEntity getUser() {
		return user;
	}	
}
