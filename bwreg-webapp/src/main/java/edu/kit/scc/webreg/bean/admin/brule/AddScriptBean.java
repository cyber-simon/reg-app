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

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.service.ScriptService;
import edu.kit.scc.webreg.util.ViewIds;

@Named("addScriptBean")
@RequestScoped
public class AddScriptBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ScriptService service;

	private ScriptEntity entity;

	@PostConstruct
	public void init() {
		entity = service.createNew();
	}

	public String save() {
		entity = service.save(entity);
		return ViewIds.EDIT_SCRIPT + "?scriptId=" + entity.getId() + "&faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.LIST_SCRIPTS + "?faces-redirect=true";
	}

	public ScriptEntity getEntity() {
		return entity;
	}

	public void setEntity(ScriptEntity entity) {
		this.entity = entity;
	}
}
