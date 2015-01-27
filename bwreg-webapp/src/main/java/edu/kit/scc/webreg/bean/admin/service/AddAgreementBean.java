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
package edu.kit.scc.webreg.bean.admin.service;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.AgreementTextEntity;
import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.service.AgreementTextService;
import edu.kit.scc.webreg.service.PolicyService;

@ManagedBean
@ViewScoped
public class AddAgreementBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PolicyService policyService;

	@Inject
	private AgreementTextService service;
	
	private PolicyEntity policy;
	
	private AgreementTextEntity entity;
	
	private Long policyId;

	public void preRenderView(ComponentSystemEvent ev) {
		policy = policyService.findById(policyId);
		entity = service.createNew();
	}
	
	public String save() {
		entity.setPolicy(policy);
		service.save(entity);
		return "show-policy.xhtml?faces-redirect=true&id=" + policy.getId();
	}

	public PolicyEntity getPolicy() {
		return policy;
	}

	public void setPolicy(PolicyEntity policy) {
		this.policy = policy;
	}

	public AgreementTextEntity getEntity() {
		return entity;
	}

	public void setEntity(AgreementTextEntity entity) {
		this.entity = entity;
	}

	public Long getPolicyId() {
		return policyId;
	}

	public void setPolicyId(Long policyId) {
		this.policyId = policyId;
	}
	
}
