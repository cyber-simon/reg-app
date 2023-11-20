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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.BusinessRuleEntity;
import edu.kit.scc.webreg.service.BusinessRuleService;
import edu.kit.scc.webreg.util.ViewIds;

@Named("addBusinessRuleBean")
@RequestScoped
public class AddBusinessRuleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private BusinessRuleService service;

	private BusinessRuleEntity entity;

	@PostConstruct
	public void init() {
		entity = service.createNew();
	}

	public String save() {
		entity = service.save(entity);
		return ViewIds.EDIT_BUSINESS_RULE + "?ruleId=" + entity.getId() + "&faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.LIST_BUSINESS_RULES + "?faces-redirect=true";
	}

	public BusinessRuleEntity getEntity() {
		return entity;
	}

	public void setEntity(BusinessRuleEntity entity) {
		this.entity = entity;
	}
}
