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
package edu.kit.scc.webreg.bean.sadm.group;

import java.io.Serializable;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.LocalGroupService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class GroupAdminAddLocalGroupBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private LocalGroupService service;

	@Inject
	private ServiceService serviceService;

	@Inject
	private AuthorizationBean authBean;

	private LocalGroupEntity entity;

	private ServiceEntity serviceEntity;

	private Long serviceId;

	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceEntity == null)
			serviceEntity = serviceService.fetch(serviceId);

		if (!authBean.isUserServiceGroupAdmin(serviceEntity))
			throw new NotAuthorizedException("Nicht autorisiert");

		if (entity == null)
			entity = service.createNew(serviceEntity);

	}

	public String save() {

		entity = service.save(entity, serviceEntity);

		return ViewIds.GROUP_ADMIN_INDEX + "?serviceId=" + serviceEntity.getId() + "&faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.GROUP_ADMIN_INDEX + "?serviceId=" + serviceEntity.getId() + "&faces-redirect=true";
	}

	public LocalGroupEntity getEntity() {
		return entity;
	}

	public void setEntity(LocalGroupEntity entity) {
		this.entity = entity;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public ServiceEntity getServiceEntity() {
		return serviceEntity;
	}
}
