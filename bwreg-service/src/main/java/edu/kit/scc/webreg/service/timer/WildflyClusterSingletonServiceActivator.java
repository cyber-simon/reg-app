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

import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.clustering.singleton.SingletonServiceBuilderFactory;

public class WildflyClusterSingletonServiceActivator implements ServiceActivator {
    
	private final Logger logger = LoggerFactory.getLogger(WildflyClusterSingletonServiceActivator.class);

    @Override
    public void activate(ServiceActivatorContext context) {
    	logger.info("ClusterSingletonService will be installed!");

    	ClusterSingletonTimerService service = new ClusterSingletonTimerService();
    	ServiceController<?> factoryService = context.getServiceRegistry().getRequiredService(SingletonServiceBuilderFactory.SERVICE_NAME.append("server", "default"));
    	SingletonServiceBuilderFactory factory = (SingletonServiceBuilderFactory) factoryService.getValue();
    	
    	factory.createSingletonServiceBuilder(ClusterSingletonTimerService.SINGLETON_SERVICE_NAME, service)
    		.build(context.getServiceTarget())
    			.addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, service.env)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();
    }
}
