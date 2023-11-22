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
package edu.kit.scc.webreg.bean.admin.brule;

import java.io.Serializable;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.service.ScriptService;

@Named("showScriptBean")
@RequestScoped
public class ShowScriptBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ScriptService service;

	private ScriptEntity entity;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.fetch(id);
	}
	
	public ScriptEntity getEntity() {
		return entity;
	}

	public void setEntity(ScriptEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
