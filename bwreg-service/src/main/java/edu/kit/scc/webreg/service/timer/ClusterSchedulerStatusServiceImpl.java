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

import javax.ejb.Stateless;

import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;

@Stateless
public class ClusterSchedulerStatusServiceImpl implements
		ClusterSchedulerStatusService, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public String getActiveNode() {
		return getClusterScheduler().getNodeName();
	}
	
	@Override
	public ClusterScheduler getClusterScheduler() {
		ServiceController<?> serviceController = 
				CurrentServiceContainer.getServiceContainer().getService(
						ClusterSingletonTimerService.SINGLETON_SERVICE_NAME);
		Service<?> service = serviceController.getService();
		return (ClusterScheduler) service.getValue();
	}
}
