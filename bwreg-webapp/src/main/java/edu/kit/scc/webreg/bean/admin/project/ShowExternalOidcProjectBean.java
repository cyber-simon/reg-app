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
package edu.kit.scc.webreg.bean.admin.project;

import java.io.Serializable;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;
import edu.kit.scc.webreg.service.project.ExternalOidcProjectService;

@Named
@ViewScoped
public class ShowExternalOidcProjectBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private ExternalOidcProjectService service;

	private ExternalOidcProjectEntity entity;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ExternalOidcProjectEntity getEntity() {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, "projectServices");
		}

		return entity;
	}

	public void setEntity(ExternalOidcProjectEntity entity) {
		this.entity = entity;
	}
}
