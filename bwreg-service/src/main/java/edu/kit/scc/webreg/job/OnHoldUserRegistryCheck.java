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
package edu.kit.scc.webreg.job;

import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.service.UserService;


public class OnHoldUserRegistryCheck extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(OnHoldUserRegistryCheck.class);

		logger.info("Starting Checking ON_HOLD user registries");
		
		try {
			InitialContext ic = new InitialContext();
			
			UserService userService = (UserService) ic.lookup("global/bwreg/bwreg-service/UserServiceImpl!edu.kit.scc.webreg.service.UserService");

			List<UserEntity> userList = userService.findByStatus(UserStatus.ON_HOLD);
			
			for (UserEntity user : userList) {
				logger.info("Checking user {}", user.getEppn());
				userService.checkOnHoldRegistries(user);
			}
		} catch (NamingException e) {
			logger.warn("Could not check ON_HOLD user registries: {}", e);
		}
	}
}
