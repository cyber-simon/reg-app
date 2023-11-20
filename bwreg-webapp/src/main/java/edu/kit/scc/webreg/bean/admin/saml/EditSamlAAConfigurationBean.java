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
import java.util.ArrayList;
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity_;
import edu.kit.scc.webreg.service.SamlAAConfigurationService;

@Named
@ViewScoped
public class EditSamlAAConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlAAConfigurationService service;

	private SamlAAConfigurationEntity entity;

	private Long id;

	private List<String> hostNameList;

	private String hostName;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, SamlAAConfigurationEntity_.hostNameList);
			hostNameList = new ArrayList<String>(entity.getHostNameList());
		}
	}

	public String save() {
		entity.setHostNameList(hostNameList);
		service.save(entity);
		return "show-aa-config.xhtml?faces-redirect=true&id=" + entity.getId();
	}

	public void addHost() {
		if (hostName != null) {
			hostNameList.add(hostName);
			hostName = null;
		}
	}

	public void removeHost(String key) {
		setHostName(key);
		hostNameList.remove(key);
	}

	public SamlAAConfigurationEntity getEntity() {
		return entity;
	}

	public void setEntity(SamlAAConfigurationEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<String> getHostNameList() {
		return hostNameList;
	}

	public void setHostNameList(List<String> hostNameList) {
		this.hostNameList = hostNameList;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
}
