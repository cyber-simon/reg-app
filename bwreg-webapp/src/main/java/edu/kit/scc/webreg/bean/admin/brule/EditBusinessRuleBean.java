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
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.BusinessRuleEntity;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.service.BusinessRulePackageService;
import edu.kit.scc.webreg.service.BusinessRuleService;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class EditBusinessRuleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private BusinessRuleService service;
	
	@Inject
	private BusinessRulePackageService packageService;
	
	private BusinessRuleEntity entity;
	
	private List<BusinessRulePackageEntity> packageList;
	private BusinessRulePackageEntity selectedPackage;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.findById(id);
		packageList = packageService.findAll();
		selectedPackage = entity.getRulePackage();
	}
	
	public String save() {
		entity.setRulePackage(selectedPackage);
		service.save(entity);
		return ViewIds.SHOW_BUSINESS_RULE + "?faces-redirect=true&ruleId=" + entity.getId();
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<BusinessRulePackageEntity> getPackageList() {
		return packageList;
	}

	public BusinessRulePackageEntity getSelectedPackage() {
		return selectedPackage;
	}

	public void setSelectedPackage(BusinessRulePackageEntity selectedPackage) {
		this.selectedPackage = selectedPackage;
	}

	
}
