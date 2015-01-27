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
import edu.kit.scc.webreg.service.AgreementTextService;

@ManagedBean
@ViewScoped
public class EditAgreementBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private AgreementTextService service;
	
	private AgreementTextEntity entity;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findById(id);
		}
	}
	
	public String save() {
		service.save(entity);
		return "show-policy.xhtml?faces-redirect=true&id=" + entity.getPolicy().getId();
	}

	public AgreementTextEntity getEntity() {
		return entity;
	}

	public void setEntity(AgreementTextEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
}
