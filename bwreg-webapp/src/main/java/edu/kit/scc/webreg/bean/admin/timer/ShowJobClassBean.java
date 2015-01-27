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

import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

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
		entity = service.findById(id);
	}
	
	public void fireJob() {
		entity = service.findById(id);
		logger.debug("Directly invoking: {} [{}]", entity.getName(), entity.getJobClassName());

		try {
			Object o = Class.forName(entity.getJobClassName()).newInstance();
			if (o instanceof ExecutableJob) {
				ExecutableJob job = (ExecutableJob) o;
				job.setJobStore(entity.getJobStore());
				asyncExecutor.execute(job);
			}
			else {
				logger.warn("Could not execute job {} ({}): not instance of ExecutableJob", entity.getName(), entity.getJobClassName());
			}
		} catch (InstantiationException e) {
			logger.warn("Could not execute job {} ({}): {}",  entity.getName(), entity.getJobClassName(), e.toString());
		} catch (IllegalAccessException e) {
			logger.warn("Could not execute job {} ({}): {}",  entity.getName(), entity.getJobClassName(), e.toString());
		} catch (ClassNotFoundException e) {
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
