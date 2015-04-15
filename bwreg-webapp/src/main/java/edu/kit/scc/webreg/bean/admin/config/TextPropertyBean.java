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
package edu.kit.scc.webreg.bean.admin.config;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.TextPropertyEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.TextPropertyService;

@ManagedBean
@ViewScoped
public class TextPropertyBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private TextPropertyService service;
	
	private Boolean initialized = false;
	
	private LazyDataModel<TextPropertyEntity> list;

	private String newKey, newLang, newValue;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			init();
			initialized = true;
		}
	}
	
	private void init() {
		list = new GenericLazyDataModelImpl<TextPropertyEntity, TextPropertyService, Long>(service);
	}

	public void save(TextPropertyEntity tp) {
		tp = service.save(tp);
		init();
	}
	
	public void create() {
		TextPropertyEntity tp = service.createNew();
		tp.setKey(newKey);
		tp.setLanguage(newLang);
		tp.setValue(newValue);
		tp = service.save(tp);
	}

	public void delete(TextPropertyEntity tp) {
		newKey = tp.getKey();
		newLang = tp.getLanguage();
		newValue = tp.getValue();
		service.delete(tp);
	}
	
	public LazyDataModel<TextPropertyEntity> getList() {
		return list;
	}

	public void setList(LazyDataModel<TextPropertyEntity> list) {
		this.list = list;
	}

	public String getNewKey() {
		return newKey;
	}

	public void setNewKey(String newKey) {
		this.newKey = newKey;
	}

	public String getNewLang() {
		return newLang;
	}

	public void setNewLang(String newLang) {
		this.newLang = newLang;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	
}
