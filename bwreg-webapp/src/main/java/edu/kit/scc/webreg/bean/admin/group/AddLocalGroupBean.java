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
package edu.kit.scc.webreg.bean.admin.group;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.service.LocalGroupService;
import edu.kit.scc.webreg.util.ViewIds;

@Named("addLocalGroupBean")
@RequestScoped
public class AddLocalGroupBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private LocalGroupService service;
	
	private LocalGroupEntity entity;
	
	@PostConstruct
	public void init() {
		entity = service.createNew();
	}
	
	public String save() {
		service.save(entity);
		return ViewIds.LIST_LOCAL_GROUPS + "?faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.LIST_LOCAL_GROUPS + "?faces-redirect=true";
	}

	public LocalGroupEntity getEntity() {
		return entity;
	}

	public void setEntity(LocalGroupEntity entity) {
		this.entity = entity;
	}
}
