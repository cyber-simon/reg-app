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

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.RegisterUserService;

public class RegistryReconsiliationExecutor extends
		AbstractEventExecutor<ServiceRegisterEvent, RegistryEntity> {

	private static final long serialVersionUID = 1L;

	public RegistryReconsiliationExecutor() {
		super();
	}

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(RegistryReconsiliationExecutor.class);
		logger.debug("Executing");
		
		Boolean fullRecon = Boolean.parseBoolean(getJobStore().get("full_recon"));
		String executor = getJobStore().get("executor");

		if (fullRecon == null) {
			logger.warn("No full_recon configured for RegistryReconsiliationExecutor. Using false");
			fullRecon = true;
		}
		
		if (executor == null) {
			logger.warn("No executor configured for RegistryReconsiliationExecutor. Using unknown");
			executor = "unknown";
		}
		
		try {
			InitialContext ic = new InitialContext();
			
			RegisterUserService registerUserService = (RegisterUserService) ic.lookup("global/bwreg/bwreg-service/RegisterUserServiceImpl!edu.kit.scc.webreg.service.reg.RegisterUserService");
			
			RegistryEntity registry = getEvent().getEntity();
			
			if (registry.getId() == null) {
				logger.info("RegistryEntity is not yet persisted. Aborting recon.");
				return;
			}
			
			try {
				registerUserService.reconsiliation(registry, fullRecon, executor);
				registerUserService.reconGroupsForRegistry(registry, executor);
			} catch (RegisterException e) {
				logger.warn("Could not recon registry {}: {}", registry.getId(), e);
			}
			
		} catch (NamingException e) {
			logger.warn("Could execute: {}", e);
		}
		
	}

}
