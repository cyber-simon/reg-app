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
package edu.kit.scc.webreg.bean.admin.service;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.JobClassEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceEventEntity;
import edu.kit.scc.webreg.service.JobClassService;
import edu.kit.scc.webreg.service.ServiceEventService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class EditServiceEventBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceService service;

	@Inject
	private ServiceEventService eventService;

	@Inject
	private JobClassService jobClassService;
	
	private ServiceEntity entity;
	
	private List<ServiceEventEntity> eventList;
	
	private List<JobClassEntity> jobClassList;
	
	private JobClassEntity selectedJobClass;
	
	private EventType selectedRegisterEventType;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findById(id);
			eventList = eventService.findAllByService(entity);
			jobClassList = jobClassService.findAll();
		}
	}

	public void addEvent() {
		ServiceEventEntity ev = eventService.createNew();
		ev.setJobClass(selectedJobClass);
		ev.setEventType(selectedRegisterEventType);
		ev.setService(entity);
		eventService.save(ev);
		eventList = eventService.findAllByService(entity);
		selectedJobClass = null;
	}
	
	public void removeEvent(ServiceEventEntity ev) {
		selectedJobClass = ev.getJobClass();
		selectedRegisterEventType = ev.getEventType();
		eventService.delete(ev);
		eventList = eventService.findAllByService(entity);
	}
	
	public String back() {
		return ViewIds.SHOW_SERVICE + "?faces-redirect=true&id=" + entity.getId();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ServiceEntity getEntity() {
		return entity;
	}

	public void setEntity(ServiceEntity entity) {
		this.entity = entity;
	}

	public List<ServiceEventEntity> getEventList() {
		return eventList;
	}

	public EventType[] getServiceRegisterEventTypes() {
		return EventType.values();
	}

	public List<JobClassEntity> getJobClassList() {
		return jobClassList;
	}

	public void setJobClassList(List<JobClassEntity> jobClassList) {
		this.jobClassList = jobClassList;
	}

	public JobClassEntity getSelectedJobClass() {
		return selectedJobClass;
	}

	public void setSelectedJobClass(JobClassEntity selectedJobClass) {
		this.selectedJobClass = selectedJobClass;
	}

	public EventType getSelectedRegisterEventType() {
		return selectedRegisterEventType;
	}

	public void setSelectedRegisterEventType(
			EventType selectedRegisterEventType) {
		this.selectedRegisterEventType = selectedRegisterEventType;
	}
}
