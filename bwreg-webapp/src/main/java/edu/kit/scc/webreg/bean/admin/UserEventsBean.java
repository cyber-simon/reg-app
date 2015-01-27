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
package edu.kit.scc.webreg.bean.admin;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.JobClassEntity;
import edu.kit.scc.webreg.entity.UserEventEntity;
import edu.kit.scc.webreg.service.JobClassService;
import edu.kit.scc.webreg.service.UserEventService;

@ManagedBean
@ViewScoped
public class UserEventsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private UserEventService eventService;

	@Inject
	private JobClassService jobClassService;
	
	private List<UserEventEntity> eventList;
	
	private List<JobClassEntity> jobClassList;
	
	private JobClassEntity selectedJobClass;
	
	private EventType selectedUserEventType;
	
	private Boolean initialized = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			eventList = eventService.findAll();
			jobClassList = jobClassService.findAll();
		}
	}

	public void addEvent() {
		UserEventEntity ev = eventService.createNew();
		ev.setJobClass(selectedJobClass);
		ev.setEventType(selectedUserEventType);
		eventService.save(ev);
		eventList = eventService.findAll();
		selectedJobClass = null;
	}
	
	public void removeEvent(UserEventEntity ev) {
		selectedJobClass = ev.getJobClass();
		selectedUserEventType = ev.getEventType();
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

	public List<UserEventEntity> getEventList() {
		return eventList;
	}

	public void setEventList(List<UserEventEntity> eventList) {
		this.eventList = eventList;
	}

	public EventType getSelectedUserEventType() {
		return selectedUserEventType;
	}

	public void setSelectedUserEventType(EventType selectedUserEventType) {
		this.selectedUserEventType = selectedUserEventType;
	}

}
