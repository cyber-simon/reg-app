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
import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.service.BusinessRulePackageService;

@Named("showRulePackageBean")
@RequestScoped
public class ShowRulePackageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private BusinessRulePackageService service;

	private BusinessRulePackageEntity entity;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.findById(id);
	}
	
	public void markForReload() {
		entity = service.findById(id);
		entity.setDirtyStamp(new Date());
		entity = service.save(entity);
	}
	
	public BusinessRulePackageEntity getEntity() {
		return entity;
	}

	public void setEntity(BusinessRulePackageEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
