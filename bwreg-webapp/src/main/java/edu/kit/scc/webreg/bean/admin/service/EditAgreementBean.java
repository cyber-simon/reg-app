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
package edu.kit.scc.webreg.bean.admin.service;

import java.io.Serializable;
import java.util.HashMap;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.AgreementTextEntity;
import edu.kit.scc.webreg.service.AgreementTextService;

@Named
@ViewScoped
public class EditAgreementBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private AgreementTextService service;
	
	private AgreementTextEntity entity;
	
	private Long id;
	
	private String inputFieldLang, inputFieldText;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.fetch(id);
		}
	}
	
	public String save() {
		service.save(entity);
		return "show-policy.xhtml?faces-redirect=true&id=" + entity.getPolicy().getId();
	}

	public void addText() {
		if (entity.getAgreementMap() == null) {
			entity.setAgreementMap(new HashMap<String, String>());
		}
		entity.getAgreementMap().put(inputFieldLang, inputFieldText);
		inputFieldLang = "";
		inputFieldText = "";
	}
	
	public void removeText(String key) {
		inputFieldLang = key;
		inputFieldText = entity.getAgreementMap().remove(key);
	}
	
	public AgreementTextEntity getEntity() {
		return entity;
	}

	public void setEntity(AgreementTextEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getInputFieldLang() {
		return inputFieldLang;
	}

	public void setInputFieldLang(String inputFieldLang) {
		this.inputFieldLang = inputFieldLang;
	}

	public String getInputFieldText() {
		return inputFieldText;
	}

	public void setInputFieldText(String inputFieldText) {
		this.inputFieldText = inputFieldText;
	}
}
