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
package edu.kit.scc.webreg.bean.admin.as;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.service.AttributeSourceService;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class AddAttributeSourceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private AttributeSourceService service;
	
	private AttributeSourceEntity entity;
	
	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.createNew();
	}

	public String save() {
		entity = service.save(entity);
		return ViewIds.EDIT_ATTRIBUTE_SOURCE + "?id=" + entity.getId() + "&faces-redirect=true";
	}

	public AttributeSourceEntity getEntity() {
		return entity;
	}

	public void setEntity(AttributeSourceEntity entity) {
		this.entity = entity;
	}
}
