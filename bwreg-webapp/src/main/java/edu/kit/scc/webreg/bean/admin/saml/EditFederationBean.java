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
package edu.kit.scc.webreg.bean.admin.saml;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.service.BusinessRulePackageService;
import edu.kit.scc.webreg.service.FederationService;

@ManagedBean
@ViewScoped
public class EditFederationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FederationService service;
	
	@Inject
	private BusinessRulePackageService rulePackageService;

	private FederationEntity entity;

	private BusinessRulePackageEntity selectedRulePackage;
	private List<BusinessRulePackageEntity> rulePackageList;

	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findById(id);
			
			rulePackageList = rulePackageService.findAll();
			selectedRulePackage = entity.getEntityFilterRulePackage();
		}
	}
	
	public String save() {
		entity.setEntityFilterRulePackage(selectedRulePackage);
		entity = service.save(entity);
		return "show-federation.xhtml?faces-redirect=true&id=" + entity.getId();
	}

	public FederationEntity getEntity() {
		return entity;
	}

	public void setEntity(FederationEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BusinessRulePackageEntity getSelectedRulePackage() {
		return selectedRulePackage;
	}

	public void setSelectedRulePackage(BusinessRulePackageEntity selectedRulePackage) {
		this.selectedRulePackage = selectedRulePackage;
	}

	public List<BusinessRulePackageEntity> getRulePackageList() {
		return rulePackageList;
	}

	public void setRulePackageList(List<BusinessRulePackageEntity> rulePackageList) {
		this.rulePackageList = rulePackageList;
	}
}
