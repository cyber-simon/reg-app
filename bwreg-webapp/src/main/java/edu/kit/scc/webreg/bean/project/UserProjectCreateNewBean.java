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

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@ViewScoped
public class UserProjectCreateNewBean implements Serializable {

 	private static final long serialVersionUID = 1L;

 	@Inject
 	private SessionManager session;
 	
 	@Inject
 	private ProjectService projectService;

 	@Inject
 	private LocalProjectService localProjectService;
 	
 	@Inject
 	private IdentityService identityService;
 	
 	private IdentityEntity identity;
 	private LocalProjectEntity entity;
    
	public void preRenderView(ComponentSystemEvent ev) {
	
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.findById(session.getIdentityId());
		}
		return identity;
	}

	public String save() {
		ProjectEntity project = projectService.save(entity, identity.getId());
		return "show-local-project.xhtml?id=" + project.getId();
	}

	public String cancel() {
		return "index.xhtml";
	}
	
	public LocalProjectEntity getEntity() {
		if (entity == null) {
			entity = localProjectService.createNew();
		}
		return entity;
	}

	public void setEntity(LocalProjectEntity entity) {
		this.entity = entity;
	}	
}
