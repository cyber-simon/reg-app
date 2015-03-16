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
package edu.kit.scc.webreg.bean.admin.as;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.service.AttributeSourceService;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class EditAttributeSourceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private AttributeSourceService service;
	
	private AttributeSourceEntity entity;
	
	private Map<String, String> propertyMap;
	
	private String newKey, newValue;
		
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, "asProps");
			propertyMap = new HashMap<String, String>(entity.getAsProps());
		}
	}
	
	public String save() {
		entity.setAsProps(propertyMap);
		entity = service.save(entity);
		return ViewIds.SHOW_ATTRIBUTE_SOURCE + "?faces-redirect=true&id=" + entity.getId();
	}

	public String cancel() {
		return ViewIds.SHOW_ATTRIBUTE_SOURCE + "?faces-redirect=true&id=" + entity.getId();
	}
	
	public void removeProp(String key) {
		setNewKey(key);
		setNewValue(propertyMap.get(key));
		propertyMap.remove(key);
	}
	
	public void addProp() {
		if (newKey != null && newValue != null) {
			propertyMap.put(newKey, newValue);
			setNewKey(null);
			setNewValue(null);
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}

	public void setPropertyMap(Map<String, String> propertyMap) {
		this.propertyMap = propertyMap;
	}

	public String getNewKey() {
		return newKey;
	}

	public void setNewKey(String newKey) {
		this.newKey = newKey;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public AttributeSourceEntity getEntity() {
		return entity;
	}

	public void setEntity(AttributeSourceEntity entity) {
		this.entity = entity;
	}
}
