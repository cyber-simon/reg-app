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

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.JobClassEntity;
import edu.kit.scc.webreg.entity.JobScheduleEntity;
import edu.kit.scc.webreg.service.JobClassService;
import edu.kit.scc.webreg.service.JobScheduleService;

@Named
@ViewScoped
public class WizardClearExpiredLoginInfoBean implements Serializable {

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
	
	private Boolean initialized = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			setJobName("wizard_ClearExpiredLoginInfos");
			setScheduleName("wizard_ClearExpiredLoginInfosSchedule");
			setDays(30L);
			setHours(0L);
			setScheduleTiming("5m");

			jobClass = jobClassService.findByAttr("jobClassName", "edu.kit.scc.webreg.job.ClearExpiredLoginInfo");
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
			jobClass.setName("wizard_ClearExpiredLoginInfos");
		jobClass.setSingleton(true);
		jobClass.setJobClassName("edu.kit.scc.webreg.job.ClearExpiredLoginInfo");
		jobClass.setJobStore(new HashMap<String, String>());
		jobClass.getJobStore().put("purge_millis", Long.toString((getDays() * 24L * 60L * 60L * 1000L) + (getHours() * 60L * 60L * 1000L)));
		jobClass = jobClassService.save(jobClass);
	}
	
	public void createSchedule() {
		jobSchedule = jobScheduleService.createNew();
		if (getScheduleName() != null)
			jobSchedule.setName(getScheduleName());
		else
			jobSchedule.setName("wizard_ClearExpiredLoginInfosSchedule");
		jobSchedule.setJobClass(getJobClass());
		jobSchedule.setYear("*");
		jobSchedule.setMonth("*");
		jobSchedule.setDayOfMonth("*");
		jobSchedule.setDayOfWeek("*");
		jobSchedule.setHour("*");
		if (scheduleTiming.equals("1m")) {
			jobSchedule.setMinute("0/1");
			jobSchedule.setSecond("15");
		}
		else if (scheduleTiming.equals("10m")) {
			jobSchedule.setMinute("0/10");
			jobSchedule.setSecond("15");
		}
		else {
			jobSchedule.setMinute("0/5");
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
}
