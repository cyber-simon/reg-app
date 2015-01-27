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
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.bootstrap.NodeConfiguration;
import edu.kit.scc.webreg.service.timer.ClusterSchedulerStatusService;
import edu.kit.scc.webreg.service.timer.StandardScheduler;

@Named("schedulerStatusBean")
@RequestScoped
public class SchedulerStatusBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ClusterSchedulerStatusService clusterSchedulerStatusService;

	@Inject
	private StandardScheduler standardScheduler;
	
	@Inject
	private NodeConfiguration nodeConfiguration;
	
	public ClusterSchedulerStatusService getClusterSchedulerStatusService() {
		return clusterSchedulerStatusService;
	}

	public NodeConfiguration getNodeConfiguration() {
		return nodeConfiguration;
	}

	public StandardScheduler getStandardScheduler() {
		return standardScheduler;
	}
	
	
}
