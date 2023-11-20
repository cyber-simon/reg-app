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

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.VelocityTemplateEntity;
import edu.kit.scc.webreg.service.tpl.VelocityTemplateService;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class AddTemplateBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private VelocityTemplateService service;

	private VelocityTemplateEntity entity;
	
	public String save() {
		entity = service.save(entity);
		return ViewIds.EDIT_PAGE_TEMPLATE + "?faces-redirect=true&id=" + entity.getId();
	}

	public VelocityTemplateEntity getEntity() {
		if (entity == null) {
			entity = service.createNew();
		}
		return entity;
	}

	public void setEntity(VelocityTemplateEntity entity) {
		this.entity = entity;
	}	
}
