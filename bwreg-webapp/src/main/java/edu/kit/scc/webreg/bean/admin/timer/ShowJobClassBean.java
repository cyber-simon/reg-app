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
import java.lang.reflect.InvocationTargetException;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.JobClassEntity;
import edu.kit.scc.webreg.job.ExecutableJob;
import edu.kit.scc.webreg.service.JobClassService;
import edu.kit.scc.webreg.service.timer.AsyncJobExecutor;

@Named("showJobClassBean")
@RequestScoped
public class ShowJobClassBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private JobClassService service;

	@Inject
	private Logger logger;

	@Inject
	private AsyncJobExecutor asyncExecutor;
	
	private JobClassEntity entity;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.fetch(id);
	}
	
	public void fireJob() {
		entity = service.fetch(id);
		logger.debug("Directly invoking: {} [{}]", entity.getName(), entity.getJobClassName());

		try {
			Object o = Class.forName(entity.getJobClassName()).getConstructor().newInstance();
			if (o instanceof ExecutableJob) {
				ExecutableJob job = (ExecutableJob) o;
				job.setJobStore(entity.getJobStore());
				asyncExecutor.execute(job);
			}
			else {
				logger.warn("Could not execute job {} ({}): not instance of ExecutableJob", entity.getName(), entity.getJobClassName());
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
			logger.warn("Could not execute job {} ({}): {}",  entity.getName(), entity.getJobClassName(), e.toString());
		} 
	}
	
	public JobClassEntity getEntity() {
		return entity;
	}

	public void setEntity(JobClassEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
