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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.service.BusinessRulePackageService;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class AddRulePackageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private BusinessRulePackageService service;

	private BusinessRulePackageEntity entity;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.createNew();
		}
	}
	
	public String save() {
		entity = service.save(entity);
		return ViewIds.SHOW_RULE_PACKAGE + "?ruleId=" + entity.getId() + "&faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.LIST_BUSINESS_RULES + "?faces-redirect=true";
	}

	public BusinessRulePackageEntity getEntity() {
		return entity;
	}

	public void setEntity(BusinessRulePackageEntity entity) {
		this.entity = entity;
	}
}
