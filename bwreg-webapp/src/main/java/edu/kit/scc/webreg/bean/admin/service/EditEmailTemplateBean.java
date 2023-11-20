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

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.EmailTemplateEntity;
import edu.kit.scc.webreg.service.EmailTemplateService;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class EditEmailTemplateBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EmailTemplateService service;
	
	private EmailTemplateEntity entity;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.fetch(id);
		}
	}
	
	public String save() {
		entity = service.save(entity);
		return ViewIds.SHOW_EMAIL_TEMPLATE + "?faces-redirect=true&id=" + entity.getId();
	}

	public EmailTemplateEntity getEntity() {
		return entity;
	}

	public void setEntity(EmailTemplateEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
}
