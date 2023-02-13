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
import java.util.List;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.ssh.SshPubKeyRegistryService;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@ViewScoped
public class SshPubKeyApprovalListBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<SshPubKeyRegistryEntity> list;
    
    @Inject
    private SshPubKeyRegistryService service;

    @Inject
    private ServiceService serviceService;
    
    @Inject
    private AuthorizationBean authBean;
    
    @Inject
    private SessionManager sessionManager;
    
    private ServiceEntity serviceEntity;
    
    private Long serviceId;

    private SshPubKeyRegistryEntity selectedKey;
    private String approverComment;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceEntity == null) {
			serviceEntity = serviceService.fetch(serviceId); 
		}

		if (! authBean.isUserServiceSshPubKeyApprover(serviceEntity))
			throw new NotAuthorizedException("Nicht autorisiert");
		
	}
    
	public String approve(SshPubKeyRegistryEntity key) {
		key.setApproverComment(approverComment);
		service.approveRegistry(key, sessionManager.getIdentityId());
		list.remove(selectedKey);
		return null;
	}
	
	public String deny(SshPubKeyRegistryEntity key) {
		key.setApproverComment(approverComment);
		service.denyRegistry(key, sessionManager.getIdentityId());
		list.remove(selectedKey);
		return null;
	}
	
	public List<SshPubKeyRegistryEntity> getList() {
		if (list == null) {
			list = service.findForApproval(serviceEntity.getId());
		}
		return list;
	}

	public ServiceEntity getServiceEntity() {
		return serviceEntity;
	}

	public void setServiceEntity(ServiceEntity serviceEntity) {
		this.serviceEntity = serviceEntity;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public SshPubKeyRegistryEntity getSelectedKey() {
		return selectedKey;
	}

	public void setSelectedKey(SshPubKeyRegistryEntity selectedKey) {
		this.selectedKey = selectedKey;
	}

	public String getApproverComment() {
		return approverComment;
	}

	public void setApproverComment(String approverComment) {
		this.approverComment = approverComment;
	}

}
