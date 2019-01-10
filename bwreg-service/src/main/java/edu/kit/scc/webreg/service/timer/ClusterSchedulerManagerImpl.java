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
import java.util.Random;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.NodeConfiguration;
import edu.kit.scc.webreg.dao.ClusterMemberDao;
import edu.kit.scc.webreg.entity.ClusterMemberEntity;
import edu.kit.scc.webreg.entity.ClusterMemberStatus;
import edu.kit.scc.webreg.entity.ClusterSchedulerStatus;

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
	private ClusterMemberDao clusterMemberDao;
	
	@Inject
	private ClusterScheduler clusterScheduler;
	
	@Override
	public void initialize() {
		Random r = new Random(System.currentTimeMillis());
		
		String nodename = nodeConfiguration.getNodeName();
		logger.info("Installing ClusterSchedulerManagerImpl for Node {}", nodename);

		statusCheck();
		
		logger.info("Starting check scheduler Timer");
		TimerConfig timerConfig = new TimerConfig();
		timerConfig.setInfo("Check Scheduler Timer");
		timerConfig.setPersistent(false);
		timerService.createIntervalTimer(55000 + r.nextInt(10000), 28000 + r.nextInt(4000), timerConfig);

		timerConfig = new TimerConfig();
		timerConfig.setInfo("Check Dead Nodes");
		timerConfig.setPersistent(false);
		timerService.createIntervalTimer(5*60*1000 + r.nextInt(10000), 2*60*1000, timerConfig);
	}

	@Timeout
    public void checkScheduler(Timer timer) {
		logger.debug("Running timer: {}", timer.getInfo().toString());

		if (timer.getInfo().toString().equalsIgnoreCase("Check Scheduler Timer")) {
			ClusterMemberEntity clusterMemberEntity = statusCheck();
			
			List<ClusterMemberEntity> clusterMemberEntityList = clusterMemberDao.findBySchedulerStatus(ClusterSchedulerStatus.MASTER);
			if (clusterMemberEntityList.size() == 0) {
				// No master at the moment. Claim master
		        logger.info("No Master on the cluster found. Claiming Master.");
		        clusterMemberEntity.setClusterSchedulerStatus(ClusterSchedulerStatus.MASTER);
		        clusterMemberEntity.setLastSchedulerStatusChange(new Date());
		        clusterScheduler.startTimers(nodeConfiguration.getNodeName());
			}
			else if (clusterMemberEntityList.size() > 1) {
		        logger.info("More than one Master on the cluster found. Resetting all to Passive.");
		        for (ClusterMemberEntity cme : clusterMemberEntityList) {
		        	cme.setClusterSchedulerStatus(ClusterSchedulerStatus.PASSIVE);
		        	cme.setLastSchedulerStatusChange(new Date());
		        }
			}		
		}
		else if (timer.getInfo().toString().equalsIgnoreCase("Check Dead Nodes")) {
			List<ClusterMemberEntity> allList = clusterMemberDao.findByMemberStatus(ClusterMemberStatus.ONLINE);
			for (ClusterMemberEntity cme: allList) {
				if (System.currentTimeMillis() - cme.getLastStatusCheck().getTime() > 1000 * 60 * 10) {
					logger.warn("Online Cluster member not checked the last 10 minutes. Setting to DEAD");
		        	cme.setClusterSchedulerStatus(ClusterSchedulerStatus.PASSIVE);
		        	cme.setLastSchedulerStatusChange(new Date());
					cme.setClusterMemberStatus(ClusterMemberStatus.DEAD);
					cme.setLastStatusChange(new Date());
				}
			}
		}
	}
	
	private ClusterMemberEntity statusCheck() {
		String nodename = nodeConfiguration.getNodeName();

		ClusterMemberEntity clusterMemberEntity = clusterMemberDao.findByNodeName(nodename);
		if (clusterMemberEntity == null) {
			logger.info("First time cluster member {} is seen. Creating entry", nodename);
			clusterMemberEntity = clusterMemberDao.createNew();
			clusterMemberEntity.setNodeName(nodename);
			clusterMemberEntity.setClusterMemberStatus(ClusterMemberStatus.OFFLINE);
			clusterMemberEntity.setClusterSchedulerStatus(ClusterSchedulerStatus.PASSIVE);
			clusterMemberEntity = clusterMemberDao.persist(clusterMemberEntity);
		}
		
		if (! clusterMemberEntity.getClusterMemberStatus().equals(ClusterMemberStatus.ONLINE)) {
			logger.info("Node is {}, setting to {}", clusterMemberEntity.getClusterMemberStatus(), ClusterMemberStatus.ONLINE);
			clusterMemberEntity.setClusterMemberStatus(ClusterMemberStatus.ONLINE);
			clusterMemberEntity.setLastStatusChange(new Date());
			clusterMemberEntity.setClusterSchedulerStatus(ClusterSchedulerStatus.PASSIVE);
			clusterMemberEntity.setLastSchedulerStatusChange(new Date());
		}
		clusterMemberEntity.setLastStatusCheck(new Date());
		
		return clusterMemberEntity;
	}
	
	@PreDestroy
	public void markOffline() {
		String nodename = nodeConfiguration.getNodeName();
		logger.info("Taking ClusterSchedulerManagerImpl for Node {} Offline", nodename);
		
		ClusterMemberEntity clusterMemberEntity = clusterMemberDao.findByNodeName(nodename);
		if (clusterMemberEntity != null) {
			clusterMemberEntity.setClusterMemberStatus(ClusterMemberStatus.OFFLINE);
			clusterMemberEntity.setLastStatusChange(new Date());
			clusterMemberEntity.setClusterSchedulerStatus(ClusterSchedulerStatus.PASSIVE);
			clusterMemberEntity.setLastSchedulerStatusChange(new Date());
		}		
	}
	
}
