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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.LocalGroupService;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class GroupAdminEditLocalGroupBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private LocalGroupService service;
	
	@Inject
	private GroupService allGroupService;

	@Inject
	private ServiceGroupFlagService groupFlagService;

	@Inject
	private ServiceService serviceService;

	@Inject
    private AuthorizationBean authBean;
	
	@Inject
	private SessionManager sessionManager;
	
	@Inject
	private FacesMessageGenerator messageGenerator;

	@Inject
	private UserService userService;
	
	@Inject
	private EventSubmitter eventSubmitter;
	
	private LocalGroupEntity entity;

	private ServiceEntity serviceEntity;
	
	private List<ServiceGroupFlagEntity> groupFlagList;

	private LazyDataModel<UserEntity> userList;
	private LazyDataModel<GroupEntity> groupList;
	
	private List<UserEntity> usersInGroup;
	
	private Long serviceId;
	private Long groupId;
	
	private Boolean savePossible = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			serviceEntity = serviceService.findById(serviceId);
			entity = service.findWithUsersAndChildren(groupId);
			userList = new GenericLazyDataModelImpl<UserEntity, UserService, Long>(userService);
			groupList = new GenericLazyDataModelImpl<GroupEntity, GroupService, Long>(allGroupService);
			groupFlagList = groupFlagService.findByGroup(entity);
			if (groupFlagList.size() == 0)
				throw new NotAuthorizedException("Gruppe ist diesem Service nicht zugeordnet");
			usersInGroup = new ArrayList<UserEntity>(userService.findByGroup(entity));
		}

		if (! authBean.isUserServiceGroupAdmin(serviceEntity))
			throw new NotAuthorizedException("Nicht autorisiert");

		if (! authBean.isUserInRoles(entity.getAdminRoles())) {
			throw new NotAuthorizedException("Nicht autorisiert");
		}
	}
	
	public String save() {
		allGroupService.updateGroupMembers(entity, new HashSet<UserEntity>(usersInGroup));
		
		for (ServiceGroupFlagEntity flag : groupFlagList) {
			flag.setStatus(ServiceGroupStatus.DIRTY);
			flag = groupFlagService.save(flag);
		}
		
		HashSet<GroupEntity> gl = new HashSet<GroupEntity>();
		gl.add(entity);
		MultipleGroupEvent mge = new MultipleGroupEvent(gl);
		try {
			eventSubmitter.submit(mge, EventType.GROUP_UPDATE, "user-" + sessionManager.getUserId());
		} catch (EventSubmitException e) {
			logger.warn("Exeption", e);
		}
		
		messageGenerator.addResolvedInfoMessage("item_saved", "item_saved_long", true);
		
		savePossible = false;
		
		return ViewIds.GROUP_ADMIN_SHOW_LOCAL_GROUP + "?faces-redirect=true&serviceId=" + serviceId + "&groupId=" + groupId;
	}
	
	public String cancel() {
		savePossible = false;
		return ViewIds.GROUP_ADMIN_SHOW_LOCAL_GROUP + "?faces-redirect=true&serviceId=" + serviceId + "&groupId=" + groupId;
	}
	
	public void addUserToGroup(UserEntity user) {
		savePossible = true;
		usersInGroup.add(user);
	}
	
	public void removeUserFromGroup(UserEntity user) {
		savePossible = true;
		usersInGroup.remove(user);
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

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public List<ServiceGroupFlagEntity> getGroupFlagList() {
		return groupFlagList;
	}

	public LazyDataModel<UserEntity> getUserList() {
		return userList;
	}

	public LazyDataModel<GroupEntity> getGroupList() {
		return groupList;
	}

	public List<UserEntity> getUsersInGroup() {
		return usersInGroup;
	}

	public Boolean getSavePossible() {
		return savePossible;
	}
}
