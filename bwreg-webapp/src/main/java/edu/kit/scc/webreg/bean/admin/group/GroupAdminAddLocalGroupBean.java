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
package edu.kit.scc.webreg.bean.admin.group;

import java.io.Serializable;
import java.util.HashSet;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.LocalGroupService;
import edu.kit.scc.webreg.service.SerialService;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class GroupAdminAddLocalGroupBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private LocalGroupService service;
	
	@Inject
	private ServiceGroupFlagService groupFlagService;

	@Inject
	private ServiceService serviceService;

	@Inject
    private AuthorizationBean authBean;
	
	@Inject
	private SerialService serialService;
	
	private LocalGroupEntity entity;

	private ServiceEntity serviceEntity;
	
	private Long serviceId;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceEntity == null) 
			serviceEntity = serviceService.findById(serviceId);

		if (! authBean.isUserServiceGroupAdmin(serviceEntity))
			throw new NotAuthorizedException("Nicht autorisiert");
		
		if (entity == null)
			entity = service.createNew();

	}
	
	public String save() {
		entity.setGidNumber(serialService.next("gid-number-serial").intValue());
		
		if (entity.getAdminRoles() == null)
			entity.setAdminRoles(new HashSet<RoleEntity>());
		
		entity.getAdminRoles().add(serviceEntity.getGroupAdminRole());

		entity = service.save(entity);

		ServiceGroupFlagEntity groupFlag = groupFlagService.createNew();
		groupFlag.setService(serviceEntity);
		groupFlag.setGroup(entity);
		groupFlag.setStatus(ServiceGroupStatus.CLEAN);
		
		groupFlag = groupFlagService.save(groupFlag);
		
		return ViewIds.GROUP_ADMIN_INDEX + "?serviceId=" + serviceId + "&faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.GROUP_ADMIN_INDEX + "?serviceId=" + serviceId + "&faces-redirect=true";
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
}
