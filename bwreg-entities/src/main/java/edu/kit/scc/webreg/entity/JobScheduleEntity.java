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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "JobScheduleEntity")
@Table(name="job_schedule")
public class JobScheduleEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", nullable = false, length = 128)
	private String name;

	@ManyToOne(targetEntity = JobClassEntity.class)
	private JobClassEntity jobClass;
	
	@Column(name = "schedule_second", length = 32)
	private String second;
	
	@Column(name = "schedule_minute", length = 32)
	private String minute;
	
	@Column(name = "schedule_hour", length = 32)
	private String hour;
	
	@Column(name = "schedule_month", length = 32)
	private String month;
	
	@Column(name = "schedule_year", length = 32)
	private String year;
	
	@Column(name = "schedule_dow", length = 32)
	private String dayOfWeek;
	
	@Column(name = "schedule_dom", length = 32)
	private String dayOfMonth;

	@Column(name = "diabled")
	private Boolean disabled;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JobClassEntity getJobClass() {
		return jobClass;
	}

	public void setJobClass(JobClassEntity jobClass) {
		this.jobClass = jobClass;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public String getMinute() {
		return minute;
	}

	public void setMinute(String minute) {
		this.minute = minute;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(String dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}
}
