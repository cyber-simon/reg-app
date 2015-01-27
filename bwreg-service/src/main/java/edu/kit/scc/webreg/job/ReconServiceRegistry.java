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

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;


public class ReconServiceRegistry extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(ReconServiceRegistry.class);

		logger.info("Starting Recon Job");
		
		if (! getJobStore().containsKey("full_recon")) {
			logger.warn("ReconServiceRegistry Job is not configured correctly. full_recon Parameter is missing in JobMap");
			return;
		}

		if (! getJobStore().containsKey("service_short_name")) {
			logger.warn("ReconServiceRegistry Job is not configured correctly. service_short_name Parameter is missing in JobMap");
			return;
		}

		Boolean fullRecon = Boolean.parseBoolean(getJobStore().get("full_recon"));
		String serviceShortName = getJobStore().get("service_short_name");

		logger.debug("Recon Job configured for service {}: fullRecon is {}", serviceShortName, fullRecon);
		
		try {
			InitialContext ic = new InitialContext();
			
			ServiceService serviceService = (ServiceService) ic.lookup("global/bwreg/bwreg-service/ServiceServiceImpl!edu.kit.scc.webreg.service.ServiceService");
			RegistryService registryService = (RegistryService) ic.lookup("global/bwreg/bwreg-service/RegistryServiceImpl!edu.kit.scc.webreg.service.RegistryService");
			
			ServiceEntity service = serviceService.findByShortName(serviceShortName);
			
			if (service == null) {
				logger.warn("No service by short name {} found! Stopping Recon", serviceShortName);
				return;
			}
			
			List<RegistryEntity> registryList = registryService.findByServiceAndStatus(service, RegistryStatus.ACTIVE);
		
			logger.info("Found {} registries for service {}", registryList.size(), service.getName());
			
			RegisterUserService registerUserService = (RegisterUserService) ic.lookup("global/bwreg/bwreg-service/RegisterUserServiceImpl!edu.kit.scc.webreg.service.reg.RegisterUserService");
			
			for (RegistryEntity registry : registryList) {
				logger.info("Recon registry {}", registry.getId());
				try {
					registerUserService.reconsiliation(registry, fullRecon, "recon-job");
				} catch (RegisterException e) {
					logger.warn("Could not recon registry {}: {}", registry.getId(), e);
				}
			}
			
		} catch (NamingException e) {
			logger.warn("Could not Recon Service: {}", e);
		}
	}
}
