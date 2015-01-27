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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.server.ServerEnvironment;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterSingletonTimerService implements Service<ClusterScheduler> {
    
	private static final Logger logger = LoggerFactory.getLogger(ClusterSingletonTimerService.class);
    public static final ServiceName SINGLETON_SERVICE_NAME = ServiceName.JBOSS.append("bwidm", "cluster", "singleton", "timer");

    private final AtomicBoolean started = new AtomicBoolean(false);

    private ClusterScheduler clusterScheduler;

    final InjectedValue<ServerEnvironment> env = new InjectedValue<ServerEnvironment>();

    public ClusterScheduler getValue() throws IllegalStateException, IllegalArgumentException {
        if (! started.get()) {
            throw new IllegalStateException("Service " + this.getClass().getName() + " not yet started!");
        }

        return clusterScheduler;
    }

    public void start(StartContext arg0) throws StartException {
        
    	if (! started.compareAndSet(false, true)) {
            throw new StartException("The service is already running");
        }

        logger.info("Start Cluster Singleton timer service '" + this.getClass().getName() + "'");

        String nodeName = env.getValue().getNodeName();

        try {
        	clusterScheduler = lookupClusterScheduler();
        	clusterScheduler.startTimers(nodeName);
        } catch (NamingException e) {
            throw new StartException("Could not initialize timer", e);
        }
    }

    public void stop(StopContext arg0) {
        
    	if (!started.compareAndSet(true, false)) {
        	logger.warn("The service '" + this.getClass().getName() + "' is not active!");
        } 
    	else {
        	logger.info("Stop Cluster Singleton timer service '" + this.getClass().getName() + "'");
            try {
            	clusterScheduler = lookupClusterScheduler();
            	clusterScheduler.stopTimers();
            } catch (NamingException e) {
            	logger.error("Could not stop timer", e);
            }
        }
    }

    private ClusterScheduler lookupClusterScheduler() throws NamingException {
        InitialContext ic = new InitialContext();
        return (ClusterScheduler) ic.lookup("global/bwreg/bwreg-service/ClusterSchedulerImpl!edu.kit.scc.webreg.service.timer.ClusterScheduler");
    	
    }
}
