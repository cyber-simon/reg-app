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

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.dao.ops.RqlExpressions;
import edu.kit.scc.webreg.dao.ops.SortBy;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceEntity_;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;
import edu.kit.scc.webreg.service.ServiceService;

@Named
@ViewScoped
public class ListGroupBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private GroupService service;

	@Inject
	private ServiceService serviceService;

	@Inject
	private ServiceGroupFlagService groupFlagService;

	@Inject
	private EventSubmitter eventSubmitter;

	private LazyDataModel<GroupEntity> list;
	private Integer sizePerMessage = 50;
	private List<ServiceEntity> serviceList;
	private ServiceEntity pickedService;
	
	public void preRenderView(ComponentSystemEvent ev) {
	}

	public LazyDataModel<GroupEntity> getGroupEntityList() {
		if (list == null) {
			list = new GenericLazyDataModelImpl<GroupEntity, GroupService>(service);
		}
		return list;
	}

	public void addAllGroupFlags() {
		List<GroupEntity> groupList = service.findAll();

		for (GroupEntity group : groupList) {
			if (group instanceof HomeOrgGroupEntity) {
				ServiceBasedGroupEntity serviceBasedGroup = (HomeOrgGroupEntity) group;

				groupFlagService.createFlagIfMissing(serviceBasedGroup, pickedService);
			}
		}
	}

	public void fireDirtyGroupChangeEvent() {
		List<ServiceGroupFlagEntity> groupFlagList = groupFlagService.findByStatus(ServiceGroupStatus.DIRTY);
		groupFlagList.addAll(groupFlagService.findByStatus(ServiceGroupStatus.TO_DELETE));

		HashSet<GroupEntity> dirtyGroupSet = new HashSet<GroupEntity>();
		for (ServiceGroupFlagEntity gf : groupFlagList)
			dirtyGroupSet.add(gf.getGroup());

		List<HashSet<GroupEntity>> chunkList = new ArrayList<>();
		HashSet<GroupEntity> groups = null;
		int i = 0;
		for (GroupEntity group : dirtyGroupSet) {
			if ((i % sizePerMessage) == 0) {
				groups = new HashSet<GroupEntity>();
				chunkList.add(groups);
			}
			groups.add(group);
			i++;
		}

		for (HashSet<GroupEntity> groupSet : chunkList) {
			MultipleGroupEvent mge = new MultipleGroupEvent(groupSet);
			try {
				eventSubmitter.submit(mge, EventType.GROUP_UPDATE, "master-admin");
			} catch (EventSubmitException e) {
				logger.warn("Exeption", e);
			}
		}
	}

	public Integer getSizePerMessage() {
		return sizePerMessage;
	}

	public void setSizePerMessage(Integer sizePerMessage) {
		this.sizePerMessage = sizePerMessage;
	}

	public List<ServiceEntity> getServiceList() {
		if (serviceList == null) {
			serviceList = serviceService.findAll(PaginateBy.unlimited(), SortBy.ascendingBy("name"),
					RqlExpressions.equal(ServiceEntity_.groupCapable, true));
		}
		return serviceList;
	}

	public ServiceEntity getPickedService() {
		return pickedService;
	}

	public void setPickedService(ServiceEntity pickedService) {
		this.pickedService = pickedService;
	}
}
