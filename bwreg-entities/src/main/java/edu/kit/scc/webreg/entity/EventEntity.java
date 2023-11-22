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
package edu.kit.scc.webreg.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "EventEntity")
@Table(name = "event_table")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class EventEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = JobClassEntity.class)
	private JobClassEntity jobClass;
	
	@Enumerated(EnumType.STRING)
	private EventType eventType;

	public JobClassEntity getJobClass() {
		return jobClass;
	}

	public void setJobClass(JobClassEntity jobClass) {
		this.jobClass = jobClass;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
}
