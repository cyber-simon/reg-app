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
public class WizardUpdateFederationMetadataBean implements Serializable {

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
	
	private Boolean initialized = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			setJobName("wizard_UpdateAllFederationMetadata");
			setScheduleName("wizard_UpdateAllFederationMetadataSchedule");
			setScheduleTiming("4");

			jobClass = jobClassService.findByAttr("jobClassName", "edu.kit.scc.webreg.job.UpdateAllFederationMetadata");
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
			jobClass.setName("wizard_UpdateAllFederationMetadata");
		jobClass.setSingleton(true);
		jobClass.setJobClassName("edu.kit.scc.webreg.job.UpdateAllFederationMetadata");
		jobClass = jobClassService.save(jobClass);
	}
	
	public void createSchedule() {
		jobSchedule = jobScheduleService.createNew();
		if (getScheduleName() != null)
			jobSchedule.setName(getScheduleName());
		else
			jobSchedule.setName("wizard_UpdateAllFederationMetadataSchedule");
		jobSchedule.setJobClass(getJobClass());
		jobSchedule.setYear("*");
		jobSchedule.setMonth("*");
		jobSchedule.setDayOfMonth("*");
		jobSchedule.setDayOfWeek("*");
		jobSchedule.setMinute("15");
		jobSchedule.setSecond("0");
		if (scheduleTiming.equals("1"))
			jobSchedule.setHour("*/1");
		else if (scheduleTiming.equals("2"))
			jobSchedule.setHour("*/2");
		else if (scheduleTiming.equals("4"))
			jobSchedule.setHour("*/4");
		else
			jobSchedule.setHour("*/8");
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
}
