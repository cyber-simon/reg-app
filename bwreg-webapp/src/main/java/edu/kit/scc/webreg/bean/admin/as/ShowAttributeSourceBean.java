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

import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.service.AttributeSourceService;

@Named("showAttributeSourceBean")
@RequestScoped
public class ShowAttributeSourceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private AttributeSourceService service;

	private AttributeSourceEntity entity;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.findByIdWithAttrs(id, "asProps");
	}
		
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AttributeSourceEntity getEntity() {
		return entity;
	}

	public void setEntity(AttributeSourceEntity entity) {
		this.entity = entity;
	}
}
