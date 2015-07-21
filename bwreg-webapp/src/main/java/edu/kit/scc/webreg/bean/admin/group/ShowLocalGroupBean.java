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
import edu.kit.scc.webreg.entity.GroupStatus;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.LocalGroupService;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;

@ManagedBean
@ViewScoped
public class ShowLocalGroupBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private LocalGroupService groupService;
	
	@Inject
	private ServiceGroupFlagService groupFlagService;
	
	@Inject
	private ServiceService serviceService;
	
	@Inject
	private UserService userService;

	@Inject
	private GroupService allGroupService;
	
	@Inject
	private EventSubmitter eventSubmitter;
	
	private LocalGroupEntity entity;

	private List<ServiceGroupFlagEntity> groupFlagList;
	
	private List<ServiceEntity> serviceEntityList;
	private ServiceEntity selectedServiceEntity;

	private LazyDataModel<UserEntity> userList;
	private LazyDataModel<GroupEntity> groupList;
	
	private List<UserEntity> effectiveMemberList;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = groupService.findWithUsersAndChildren(id);
			
			userList = new GenericLazyDataModelImpl<UserEntity, UserService, Long>(userService);
			groupList = new GenericLazyDataModelImpl<GroupEntity, GroupService, Long>(allGroupService);
			effectiveMemberList = new ArrayList<UserEntity>(allGroupService.getEffectiveMembers(entity));
			
			initView();
		}
	}

	public void addGroupFlag() {
		if (entity.getGroupStatus() == null || GroupStatus.DELETED.equals(entity.getGroupStatus())) {
			entity.setGroupStatus(GroupStatus.ACTIVE);
			entity = groupService.save(entity);
			entity = groupService.findWithUsersAndChildren(id);
		}
		
		ServiceGroupFlagEntity groupFlag = groupFlagService.createNew();
		groupFlag.setGroup(entity);
		groupFlag.setService(selectedServiceEntity);
		groupFlag.setStatus(ServiceGroupStatus.DIRTY);
		groupFlag = groupFlagService.save(groupFlag);

		initView();
	}
	
	public void removeGroupFlag(ServiceGroupFlagEntity groupFlag) {
		groupFlag.setStatus(ServiceGroupStatus.TO_DELETE);
		groupFlag = groupFlagService.save(groupFlag);

		initView();
	}

	public void fireGroupChangeEvent() {
		HashSet<GroupEntity> gl = new HashSet<GroupEntity>();
		gl.add(entity);
		MultipleGroupEvent mge = new MultipleGroupEvent(gl);
		try {
			eventSubmitter.submit(mge, EventType.GROUP_UPDATE, "master-admin");
		} catch (EventSubmitException e) {
			logger.warn("Exeption", e);
		}
	}

	public void addUserToGroup(UserEntity user) {
		allGroupService.addUserToGroup(user, entity);
		
		for (ServiceGroupFlagEntity flag : groupFlagList) {
			flag.setStatus(ServiceGroupStatus.DIRTY);
			groupFlagService.save(flag);
		}

		entity = groupService.findWithUsersAndChildren(id);
		effectiveMemberList = new ArrayList<UserEntity>(allGroupService.getEffectiveMembers(entity));
	}
		
	public void removeUserFromGroup(UserEntity user) {
		allGroupService.removeUserGromGroup(user, entity);

		for (ServiceGroupFlagEntity flag : groupFlagList) {
			flag.setStatus(ServiceGroupStatus.DIRTY);
			groupFlagService.save(flag);
		}

		entity = groupService.findWithUsersAndChildren(id);
		effectiveMemberList = new ArrayList<UserEntity>(allGroupService.getEffectiveMembers(entity));
	}
	
	public void addGroupToGroup(GroupEntity group) {
		entity.getChildren().add(group);
		entity = groupService.save(entity);
		
		for (ServiceGroupFlagEntity flag : groupFlagList) {
			flag.setStatus(ServiceGroupStatus.DIRTY);
			groupFlagService.save(flag);
		}
		effectiveMemberList = new ArrayList<UserEntity>(allGroupService.getEffectiveMembers(entity));
	}

	public void removeGroupFromGroup(GroupEntity group) {
		entity.getChildren().remove(group);
		entity = groupService.save(entity);

		for (ServiceGroupFlagEntity flag : groupFlagList) {
			flag.setStatus(ServiceGroupStatus.DIRTY);
			groupFlagService.save(flag);
		}
		effectiveMemberList = new ArrayList<UserEntity>(allGroupService.getEffectiveMembers(entity));
	}
	
	private void initView() {
		groupFlagList = groupFlagService.findByGroup(entity);
		serviceEntityList = serviceService.findAll();
		for (ServiceGroupFlagEntity gf : groupFlagList) {
			if (serviceEntityList.contains(gf.getService()))
				serviceEntityList.remove(gf.getService());
		}
	}
	
	public void deleteGroup() {
		entity.setGroupStatus(GroupStatus.DELETED);

		for (ServiceGroupFlagEntity flag : groupFlagList) {
			flag.setStatus(ServiceGroupStatus.TO_DELETE);
			groupFlagService.save(flag);
		}

		entity = groupService.save(entity);
		
		fireGroupChangeEvent();
		
		entity = groupService.findWithUsersAndChildren(id);
		effectiveMemberList = new ArrayList<UserEntity>(allGroupService.getEffectiveMembers(entity));
		initView();
	}
	
	public void removeAllMembers() {
		
		for (GroupEntity subGroup : entity.getChildren())
			removeGroupFromGroup(subGroup);
		
		for (UserGroupEntity userGroup : entity.getUsers())
			removeUserFromGroup(userGroup.getUser());
		
		fireGroupChangeEvent();
		
		entity = groupService.findWithUsersAndChildren(id);
		effectiveMemberList = new ArrayList<UserEntity>(allGroupService.getEffectiveMembers(entity));
		initView();
	}
	

	public LocalGroupEntity getEntity() {
		return entity;
	}

	public void setEntity(LocalGroupEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ServiceGroupFlagEntity> getGroupFlagList() {
		return groupFlagList;
	}

	public List<ServiceEntity> getServiceEntityList() {
		return serviceEntityList;
	}

	public ServiceEntity getSelectedServiceEntity() {
		return selectedServiceEntity;
	}

	public void setSelectedServiceEntity(ServiceEntity selectedServiceEntity) {
		this.selectedServiceEntity = selectedServiceEntity;
	}

	public LazyDataModel<UserEntity> getUserList() {
		return userList;
	}

	public LazyDataModel<GroupEntity> getGroupList() {
		return groupList;
	}

	public List<UserEntity> getEffectiveMemberList() {
		return effectiveMemberList;
	}
}
