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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DualListModel;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity_;
import edu.kit.scc.webreg.service.AttributeSourceService;
import edu.kit.scc.webreg.service.AttributeSourceServiceService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class EditAttributeSourceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private AttributeSourceService service;

	@Inject
	private AttributeSourceServiceService asseService;

	@Inject
	private ServiceService serviceService;

	private AttributeSourceEntity entity;

	private Map<String, String> propertyMap;
	private DualListModel<ServiceEntity> serviceList;

	private String newKey, newValue;

	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, AttributeSourceEntity_.asProps,
					AttributeSourceEntity_.attributeSourceServices);
			propertyMap = new HashMap<String, String>(entity.getAsProps());

			serviceList = new DualListModel<ServiceEntity>();
			serviceList.setSource(entity.getAttributeSourceServices().stream().map(asse -> asse.getService())
					.collect(Collectors.toList()));
			serviceList.setTarget(serviceService.findAll().stream().filter(s -> !serviceList.getSource().contains(s))
					.collect(Collectors.toList()));
		}
	}

	public String save() {
		List<ServiceEntity> oldList = entity.getAttributeSourceServices().stream().map(asse -> asse.getService())
				.collect(Collectors.toList());

		entity.setAsProps(propertyMap);
		entity = service.save(entity);

		serviceList.getSource().stream().filter(s -> !oldList.contains(s)).forEach(s -> {
			if (logger.isTraceEnabled())
				logger.trace("new on list {}", s.getName());
			asseService.connectService(entity, s);
		});

		oldList.stream().filter(s -> !serviceList.getSource().contains(s)).forEach(s -> {
			if (logger.isTraceEnabled())
				logger.trace("remove from list {}", s.getName());
			asseService.disconnectService(entity, s);
		});

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

	public DualListModel<ServiceEntity> getServiceList() {
		return serviceList;
	}

	public void setServiceList(DualListModel<ServiceEntity> serviceList) {
		this.serviceList = serviceList;
	}
}
