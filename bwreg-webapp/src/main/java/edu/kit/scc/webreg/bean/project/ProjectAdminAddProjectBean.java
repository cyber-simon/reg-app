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
package edu.kit.scc.webreg.bean.project;

import java.io.Serializable;

import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class ProjectAdminAddProjectBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;
	
	@Inject
	private LocalProjectService service;
	
	private LocalProjectEntity entity;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null)
			entity = service.createNew();
		
	}
	
	public String save() {
		entity = service.save(entity, session.getIdentityId());
		
		return ViewIds.PROJECT_ADMIN_INDEX + "&faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.PROJECT_ADMIN_INDEX + "&faces-redirect=true";
	}

	public LocalProjectEntity getEntity() {
		return entity;
	}

	public void setEntity(LocalProjectEntity entity) {
		this.entity = entity;
	}
}
