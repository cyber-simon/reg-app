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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.RegisterUserService;

public class UserReconsiliationExecutor extends
		AbstractEventExecutor<UserEvent, UserEntity> {

	private static final long serialVersionUID = 1L;

	public UserReconsiliationExecutor() {
		super();
	}

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(UserReconsiliationExecutor.class);
		logger.debug("Executing");
		
		Boolean fullRecon = Boolean.parseBoolean(getJobStore().get("full_recon"));
		String executor = getJobStore().get("executor");

		if (executor == null) {
			logger.warn("No executor configured for UserReconsiliationExecutor. Using unknown");
			executor = "unknown";
		}
		
		try {
			InitialContext ic = new InitialContext();
			
			RegisterUserService registerUserService = (RegisterUserService) ic.lookup("global/bwreg/bwreg-service/RegisterUserServiceImpl!edu.kit.scc.webreg.service.reg.RegisterUserService");
			
			UserEntity user = getEvent().getEntity();
			
			if (user.getId() == null) {
				logger.info("User is not yet persisted. Aborting recon.");
				return;
			}
			
			try {
				registerUserService.reconsiliationByUser(user, fullRecon, executor);
			} catch (RegisterException e) {
				logger.warn("Could not recon registries for user {}: {}", user.getId(), e);
			}
		} catch (NamingException e) {
			logger.warn("Could execute: {}", e);
		}
		
	}

}
