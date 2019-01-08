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

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.bootstrap.NodeConfiguration;

@Singleton
public class ClusterSchedulerManagerImpl implements ClusterSchedulerManager, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Resource
	private TimerService timerService;

	@Inject
	private NodeConfiguration nodeConfiguration;
	
	@Inject
	private ApplicationConfig appConfig;
	
	@Override
	public void initialize() {

		logger.info("Installing ClusterSchedulerManagerImpl for Node {}", nodeConfiguration.getNodeName());
		
	}

}
