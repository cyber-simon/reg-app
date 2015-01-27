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
import edu.kit.scc.webreg.service.PolicyService;

@ManagedBean
@ViewScoped
public class EditPolicyBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PolicyService service;
	
	private PolicyEntity entity;
	
	private AgreementTextEntity selectedAgreement;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findWithAgreemets(id);
			selectedAgreement = entity.getActualAgreement();
		}
	}
	
	public String save() {
		entity.setActualAgreement(selectedAgreement);
		service.save(entity);
		return "show-policy.xhtml?faces-redirect=true&id=" + entity.getId();
	}

	public PolicyEntity getEntity() {
		return entity;
	}

	public void setEntity(PolicyEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AgreementTextEntity getSelectedAgreement() {
		return selectedAgreement;
	}

	public void setSelectedAgreement(AgreementTextEntity selectedAgreement) {
		this.selectedAgreement = selectedAgreement;
	}

	
}
