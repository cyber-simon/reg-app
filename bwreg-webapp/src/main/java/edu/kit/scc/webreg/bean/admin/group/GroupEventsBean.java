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
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEventEntity;
import edu.kit.scc.webreg.entity.JobClassEntity;
import edu.kit.scc.webreg.service.GroupEventService;
import edu.kit.scc.webreg.service.JobClassService;

@ManagedBean
@ViewScoped
public class GroupEventsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private GroupEventService eventService;

	@Inject
	private JobClassService jobClassService;
	
	private List<GroupEventEntity> eventList;
	
	private List<JobClassEntity> jobClassList;
	
	private JobClassEntity selectedJobClass;
	
	private EventType selectedEventType;
	
	private Boolean initialized = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			eventList = eventService.findAll();
			jobClassList = jobClassService.findAll();
		}
	}

	public void addEvent() {
		GroupEventEntity ev = eventService.createNew();
		ev.setJobClass(selectedJobClass);
		ev.setEventType(selectedEventType);
		eventService.save(ev);
		eventList = eventService.findAll();
		selectedJobClass = null;
	}
	
	public void removeEvent(GroupEventEntity ev) {
		selectedJobClass = ev.getJobClass();
		selectedEventType = ev.getEventType();
		eventService.delete(ev);
		eventList = eventService.findAll();
	}
	
	public EventType[] getUserEventTypes() {
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

	public List<GroupEventEntity> getEventList() {
		return eventList;
	}

	public void setEventList(List<GroupEventEntity> eventList) {
		this.eventList = eventList;
	}

	public EventType getSelectedEventType() {
		return selectedEventType;
	}

	public void setSelectedEventType(EventType selectedEventType) {
		this.selectedEventType = selectedEventType;
	}

}
