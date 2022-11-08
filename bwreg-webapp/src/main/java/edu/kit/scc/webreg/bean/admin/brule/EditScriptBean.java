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

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.service.ScriptService;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class EditScriptBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ScriptService service;
	
	private ScriptEntity entity;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
	}
	
	public String save() {
		service.save(entity);
		return ViewIds.SHOW_SCRIPT + "?faces-redirect=true&scriptId=" + getEntity().getId();
	}

	public String cancel() {
		return ViewIds.LIST_SCRIPTS + "?faces-redirect=true";
	}

	public ScriptEntity getEntity() {
		if (entity == null) {
			entity = service.findById(id);
		}
		
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
