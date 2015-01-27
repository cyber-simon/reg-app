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
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;
import edu.kit.scc.webreg.service.ServiceService;

@ManagedBean
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

	public void preRenderView(ComponentSystemEvent ev) {
		if (list == null) {
			list = new GenericLazyDataModelImpl<GroupEntity, GroupService, Long>(service);
		}
	}

    public LazyDataModel<GroupEntity> getGroupEntityList() {
   		return list;
    }

    public void addAllGroupFlags() {
    	List<GroupEntity> groupList = service.findAll();
    	List<ServiceEntity> serviceList = serviceService.findAll();
    	
    	for (GroupEntity group : groupList) {
    		if (group instanceof HomeOrgGroupEntity) {
    			ServiceBasedGroupEntity serviceBasedGroup = (HomeOrgGroupEntity) group;
    			
    			for (ServiceEntity serviceEntity : serviceList) {
    				if (serviceEntity.getGroupCapable()) {
            			List<ServiceGroupFlagEntity> flagList = groupFlagService.findByGroupAndService(serviceBasedGroup, serviceEntity);
    					if (flagList.size() > 0) {
    						logger.debug("ServiceGroupFlag for service {} and group {} exists", serviceEntity.getName(), group.getName());
    					}
    					else {
    						logger.debug("Create for service {} and group {}", serviceEntity.getName(), group.getName());
    						ServiceGroupFlagEntity flag = groupFlagService.createNew();
    						flag.setGroup(serviceBasedGroup);
    						flag.setService(serviceEntity);
    						flag.setStatus(ServiceGroupStatus.DIRTY);
    						flag = groupFlagService.save(flag);
    					}
    				}
    			}
    		}
    	}
    }
    
	public void fireDirtyGroupChangeEvent() {
		List<ServiceGroupFlagEntity> groupFlagList = groupFlagService.findByStatus(ServiceGroupStatus.DIRTY);
		groupFlagList.addAll(groupFlagService.findByStatus(ServiceGroupStatus.TO_DELETE));

		HashSet<GroupEntity> dirtyGroupList = new HashSet<GroupEntity>();
		for (ServiceGroupFlagEntity gf : groupFlagList)
			dirtyGroupList.add(gf.getGroup());
		
		MultipleGroupEvent mge = new MultipleGroupEvent(dirtyGroupList);
		try {
			eventSubmitter.submit(mge, EventType.GROUP_UPDATE, "master-admin");
		} catch (EventSubmitException e) {
			logger.warn("Exeption", e);
		}
	}    
}
