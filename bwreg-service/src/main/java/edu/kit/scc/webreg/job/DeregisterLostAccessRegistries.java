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

import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;


public class DeregisterLostAccessRegistries extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(DeregisterLostAccessRegistries.class);

		if (! getJobStore().containsKey("lost_access_since_millis")) {
			logger.warn("DeregisterLostAccessRegistries Job is not configured correctly. lost_access_since_millis Parameter is missing in JobMap");
			return;
		}
		if (! getJobStore().containsKey("service_short_name")) {
			logger.warn("DeregisterLostAccessRegistries Job is not configured correctly. service_short_name Parameter is missing in JobMap");
			return;
		}

		Long lastUpdate = Long.parseLong(getJobStore().get("lost_access_since_millis"));
		
		Integer limit;
		
		if (getJobStore().containsKey("registries_per_exec")) {
			limit = Integer.parseInt(getJobStore().get("registries_per_exec"));
		}
		else {
			limit = 1;
		}
		
		String ssn = null;
		
		if (getJobStore().containsKey("service_short_name")) {
			ssn = getJobStore().get("service_short_name");
		}

		logger.info("Starting Deregister LostAccess Registries for {}", ssn);
		
		try {
			InitialContext ic = new InitialContext();
			
			RegistryService registryService = (RegistryService) ic.lookup("global/bwreg/bwreg-service/RegistryServiceImpl!edu.kit.scc.webreg.service.RegistryService");
			RegisterUserService registerUserService = (RegisterUserService) ic.lookup("global/bwreg/bwreg-service/RegisterUserServiceImpl!edu.kit.scc.webreg.service.reg.RegisterUserService");
			
			List<RegistryEntity> registryList = registryService.findByServiceAndStatus(ssn, RegistryStatus.LOST_ACCESS, 
					new Date(System.currentTimeMillis() - lastUpdate), limit);
			
			if (registryList.size() == 0)
				logger.debug("No LostAccess registries found");
			
			for (RegistryEntity registry : registryList) {
				try {
					logger.info("Deregister Registry {}", registry.getId());
					registerUserService.deregisterUser(registry, "lost-access-reg-job");
				} catch (RegisterException e) {
					logger.info("Could not deregister", e);
				}
			}
			
		} catch (NamingException e) {
			logger.warn("Could not Deregister LostAccess Registries: {}", e);
		}
	}
}
