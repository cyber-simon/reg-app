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
package edu.kit.scc.webreg.bean.admin.tpl;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.VelocityTemplateEntity;
import edu.kit.scc.webreg.service.tpl.VelocityTemplateService;

@Named("showTemplateBean")
@RequestScoped
public class ShowTemplateBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private VelocityTemplateService service;

	private VelocityTemplateEntity entity;
	
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public VelocityTemplateEntity getEntity() {
		if (entity == null) {
			entity = service.fetch(id);
		}
		return entity;
	}

	public void setEntity(VelocityTemplateEntity entity) {
		this.entity = entity;
	}
}
