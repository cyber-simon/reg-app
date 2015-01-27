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

import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.EmailTemplateEntity;
import edu.kit.scc.webreg.service.EmailTemplateService;

@Named("showEmailTemplateBean")
@RequestScoped
public class ShowEmailTemplateBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EmailTemplateService service;

	private EmailTemplateEntity entity;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.findById(id);
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
