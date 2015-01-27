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

import org.jboss.as.clustering.singleton.SingletonService;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.msc.service.DelegatingServiceContainer;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterSingletonServiceActivator implements ServiceActivator {
    
	private final Logger logger = LoggerFactory.getLogger(ClusterSingletonServiceActivator.class);

    @Override
    public void activate(ServiceActivatorContext context) {
    	logger.info("ClusterSingletonService will be installed!");

    	ClusterSingletonTimerService service = new ClusterSingletonTimerService();
        SingletonService<ClusterScheduler> singleton = new SingletonService<ClusterScheduler>(service, ClusterSingletonTimerService.SINGLETON_SERVICE_NAME);

        singleton.build(new DelegatingServiceContainer(context.getServiceTarget(), context.getServiceRegistry()))
                .addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, service.env)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();
    }
}
