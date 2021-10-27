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
package edu.kit.scc.webreg.bean.admin.timer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.JobClassEntity;
import edu.kit.scc.webreg.entity.JobScheduleEntity;
import edu.kit.scc.webreg.service.JobClassService;
import edu.kit.scc.webreg.service.JobScheduleService;

@Named
@ViewScoped
public class WizardClearOldAuditEntriesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private JobScheduleService jobScheduleService;
	
	@Inject
	private JobClassService jobClassService;
	
	private JobClassEntity jobClass;
	private JobScheduleEntity jobSchedule;
	
	private String jobName;
	private String scheduleName;
	private String scheduleTiming;
	
	private Long days;
	private Long hours;
	private Long limit;
	
	private Boolean initialized = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			setJobName("wizard_ClearOldAuditEntries");
			setScheduleName("wizard_ClearOldAuditEntriesSchedule");
			setDays(30L);
			setHours(0L);
			setLimit(10L);
			setScheduleTiming("1m");

			jobClass = jobClassService.findByAttr("jobClassName", "edu.kit.scc.webreg.job.ClearAuditLogs");
			if (jobClass != null) {
				List<JobScheduleEntity> jsList = jobScheduleService.findAllByAttr("jobClass", jobClass);
				if (jsList.size() > 0) {
					jobSchedule = jsList.get(0);
				}
			}
			initialized = true;
		}
	}

	public void createJobClass() {
		jobClass = jobClassService.createNew();
		if (getJobName() != null)
			jobClass.setName(getJobName());
		else
			jobClass.setName("wizard_ClearOldAuditEntries");
		jobClass.setSingleton(true);
		jobClass.setJobClassName("edu.kit.scc.webreg.job.ClearAuditLogs");
		jobClass.setJobStore(new HashMap<String, String>());
		jobClass.getJobStore().put("limit", Long.toString(getLimit()));
		jobClass.getJobStore().put("purge_millis", Long.toString((getDays() * 24L * 60L * 60L * 1000L) + (getHours() * 60L * 60L * 1000L)));
		jobClass = jobClassService.save(jobClass);
	}
	
	public void createSchedule() {
		jobSchedule = jobScheduleService.createNew();
		if (getScheduleName() != null)
			jobSchedule.setName(getScheduleName());
		else
			jobSchedule.setName("wizard_ClearOldAuditEntriesSchedule");
		jobSchedule.setJobClass(getJobClass());
		jobSchedule.setYear("*");
		jobSchedule.setMonth("*");
		jobSchedule.setDayOfMonth("*");
		jobSchedule.setDayOfWeek("*");
		jobSchedule.setHour("*");
		if (scheduleTiming.equals("30s")) {
			jobSchedule.setMinute("*");
			jobSchedule.setSecond("15/30");
		}
		else if (scheduleTiming.equals("2m")) {
			jobSchedule.setMinute("0/2");
			jobSchedule.setSecond("15");
		}
		else {
			jobSchedule.setMinute("0/1");
			jobSchedule.setSecond("15");
		}
		jobSchedule.setDisabled(false);
		jobSchedule = jobScheduleService.save(jobSchedule);
	}
	
	public JobClassEntity getJobClass() {
		return jobClass;
	}

	public JobScheduleEntity getJobSchedule() {
		return jobSchedule;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getScheduleTiming() {
		return scheduleTiming;
	}

	public void setScheduleTiming(String scheduleTiming) {
		this.scheduleTiming = scheduleTiming;
	}

	public Long getDays() {
		return days;
	}

	public void setDays(Long days) {
		this.days = days;
	}

	public Long getHours() {
		return hours;
	}

	public void setHours(Long hours) {
		this.hours = hours;
	}

	public Long getLimit() {
		return limit;
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}
}
