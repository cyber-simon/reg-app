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
package edu.kit.scc.webreg.event;

import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.RegisterUserService;

public class GroupReconsiliationExecutor extends
		AbstractEventExecutor<MultipleGroupEvent, HashSet<GroupEntity>> {

	private static final long serialVersionUID = 1L;

	public GroupReconsiliationExecutor() {
		super();
	}

		@Override
		public void execute() {
	
			Logger logger = LoggerFactory.getLogger(GroupReconsiliationExecutor.class);
			logger.debug("Executing");
			
			Boolean fullRecon = Boolean.parseBoolean(getJobStore().get("full_recon"));
			String executor = getJobStore().get("executor");
	
			if (fullRecon == null) {
				logger.warn("No full_recon configured for GroupReconsiliationExecutor. Using false");
				fullRecon = false;
			}
			
			if (executor == null) {
				logger.warn("No executor configured for GroupReconsiliationExecutor. Using unknown");
				executor = "unknown";
			}
			
			try {
				InitialContext ic = new InitialContext();
				
				RegisterUserService registerUserService = (RegisterUserService) ic.lookup("global/bwreg/bwreg-service/RegisterUserServiceImpl!edu.kit.scc.webreg.service.reg.RegisterUserService");
	
				Set<GroupEntity> groupList = getEvent().getEntity();
				
				try {
					registerUserService.updateGroups(groupList, executor);
				} catch (RegisterException e) {
					logger.warn("Could not update groups ", e);
				}			
				
			} catch (NamingException e) {
				logger.warn("Could execute: {}", e);
			}
			
		}
}
