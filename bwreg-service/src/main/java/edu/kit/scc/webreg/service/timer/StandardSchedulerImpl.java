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
package edu.kit.scc.webreg.service.timer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.bootstrap.NodeConfiguration;
import edu.kit.scc.webreg.drools.BpmProcessService;
import edu.kit.scc.webreg.entity.JobClassEntity;
import edu.kit.scc.webreg.entity.JobScheduleEntity;
import edu.kit.scc.webreg.job.ExecutableJob;
import edu.kit.scc.webreg.service.JobClassService;
import edu.kit.scc.webreg.service.JobScheduleService;
import edu.kit.scc.webreg.service.impl.HookManager;

@Singleton
public class StandardSchedulerImpl implements StandardScheduler, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Resource
	private TimerService timerService;

	@Inject
	private JobScheduleService jobScheduleService;
	
	@Inject
	private JobClassService jobClassService;
	
	@Inject
	private NodeConfiguration nodeConfiguration;
	
	@Inject
	private AsyncJobExecutor asyncExecutor;
	
	@Inject
	private BpmProcessService bpmProcessService;

	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private HookManager hookManager;
	
	@Override
	public void initialize() {

		for (Timer t : timerService.getTimers()) {
			cancelTimer(t);
		}

		logger.info("Installing StandardScheduler...");
		
		for (JobScheduleEntity jobSchedule : jobScheduleService.findAllBySingleton(false, false)) {
			setupTimer(jobSchedule);
		}
		
		nodeConfiguration.setTimerConfigured(new Date());
		
		createConfigTimer();
	}

	@Timeout
	public void execute(Timer timer) {
		
		if (timer.getInfo().equals("Reload Config")) {
			reloadConfig();
			return;
		}
		else if (! (timer.getInfo() instanceof JobScheduleEntity)) {
			logger.warn("Timer {} not of type JobScheduleEntity. Doing nothing", timer.getInfo());
			return;
		}
		
		JobScheduleEntity jobSchedule = (JobScheduleEntity) timer.getInfo();
		JobClassEntity jobClass = jobClassService.findById(jobSchedule.getJobClass().getId());
		
		logger.debug("----StandardScheduler invokes: {} [{}]", jobClass.getName(), jobClass.getJobClassName());

		try {
			Object o = Class.forName(jobClass.getJobClassName()).newInstance();
			if (o instanceof ExecutableJob) {
				ExecutableJob job = (ExecutableJob) o;
				job.setJobStore(jobClass.getJobStore());
				asyncExecutor.execute(job);
			}
			else {
				logger.warn("Could not execute job {} ({}): not instance of ExecutableJob", jobClass.getName(), jobClass.getJobClassName());
			}
		} catch (InstantiationException e) {
			logger.warn("Could not execute job {} ({}): {}",  jobClass.getName(), jobClass.getJobClassName(), e.toString());
		} catch (IllegalAccessException e) {
			logger.warn("Could not execute job {} ({}): {}",  jobClass.getName(), jobClass.getJobClassName(), e.toString());
		} catch (ClassNotFoundException e) {
			logger.warn("Could not execute job {} ({}): {}",  jobClass.getName(), jobClass.getJobClassName(), e.toString());
		}
	}
	
	@PreDestroy
	public void destroyTimers() {
		for (Timer t : timerService.getTimers()) {
			logger.info("Cancelling timer: {}", t.getInfo());
			t.cancel();
		}
	}

	@Override
	public TimerService getTimerService() {
		return timerService;
	}

	private void reloadConfig() {

		List<JobScheduleEntity> changedJobs = 	
				jobScheduleService.findAllBySingletonNewer(false, nodeConfiguration.getTimerConfigured());

		nodeConfiguration.setTimerConfigured(new Date());
		
		for (JobScheduleEntity jobSchedule : changedJobs) {
			logger.info("Reloading scheduled timer {}", jobSchedule.getName());
			for (Timer t : timerService.getTimers()) {
				if (t.getInfo().equals(jobSchedule)) {
					logger.debug("Cacnelling old schedule");
					t.cancel();
				}
			}
				
			if (jobSchedule.getDisabled() == false) {
				setupTimer(jobSchedule);
			}
			else {
				logger.info("Schedule is disabled, skipping");
			}
		}
		
		// Reload rules here
		bpmProcessService.reload();
		
		// Reload App Config here
		boolean reloaded = appConfig.reload();
		
		// Reload Hooks if app config was reloaded
		if (reloaded) {
			hookManager.reloadHooks();
		}

	}
	
	private void cancelTimer(Timer t) {
		logger.info("Cancelling timer: {}", t.getInfo());
		t.cancel();		
	}
	
	private void setupTimer(JobScheduleEntity jobSchedule) {
		ScheduleExpression expression = new ScheduleExpression();
		
		if (jobSchedule.getSecond() != null)
			expression.second(jobSchedule.getSecond());
		if (jobSchedule.getMinute() != null)
			expression.minute(jobSchedule.getMinute());
		if (jobSchedule.getHour() != null)
			expression.hour(jobSchedule.getHour());
		if (jobSchedule.getDayOfWeek() != null)
			expression.dayOfWeek(jobSchedule.getDayOfWeek());
		if (jobSchedule.getDayOfMonth() != null)
			expression.dayOfMonth(jobSchedule.getDayOfMonth());
		if (jobSchedule.getMonth() != null)
			expression.month(jobSchedule.getMonth());
		if (jobSchedule.getYear() != null)
			expression.year(jobSchedule.getYear());
		
		logger.info("Scheduling {} ({}): {}", jobSchedule.getName(), jobSchedule.getJobClass(),
				expression.toString());
		
		TimerConfig timerConfig = new TimerConfig();
		timerConfig.setInfo(jobSchedule);
		timerConfig.setPersistent(false);
		timerService.createCalendarTimer(expression, timerConfig);
	}
	
	private void createConfigTimer() {
		TimerConfig timerConfig = new TimerConfig();
		timerConfig.setInfo("Reload Config");
		timerConfig.setPersistent(false);
		timerService.createIntervalTimer(new Date(), 30 * 1000L, timerConfig);
	}
}
