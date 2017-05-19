package edu.kit.scc.webreg.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;

public abstract class AbstractDeregisterRegistries extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	public void executeDeregister(RegistryStatus registryStatus, Long lastUpdate, Long lastUserUpdate) {
		Logger logger = LoggerFactory.getLogger(AbstractDeregisterRegistries.class);

		if (! getJobStore().containsKey("service_short_name")) {
			logger.warn("AbstractDeregisterRegistries Job is not configured correctly. service_short_name Parameter is missing in JobMap");
			return;
		}

		String ssn = getJobStore().get("service_short_name");

		Integer limit = 1;
		if (getJobStore().containsKey("registries_per_exec"))
			limit = Integer.parseInt(getJobStore().get("registries_per_exec"));

		logger.info("Starting Deregister {} Registries for {}", registryStatus, ssn);
		
		try {
			InitialContext ic = new InitialContext();
			
			RegistryService registryService = (RegistryService) ic.lookup("global/bwreg/bwreg-service/RegistryServiceImpl!edu.kit.scc.webreg.service.RegistryService");
			RegisterUserService registerUserService = (RegisterUserService) ic.lookup("global/bwreg/bwreg-service/RegisterUserServiceImpl!edu.kit.scc.webreg.service.reg.RegisterUserService");
	
			UserService userService = (UserService) ic.lookup("global/bwreg/bwreg-service/UserServiceImpl!edu.kit.scc.webreg.service.UserService");
			KnowledgeSessionService knowledgeSessionService = (KnowledgeSessionService) ic.lookup("global/bwreg/bwreg-service/KnowledgeSessionServiceImpl!edu.kit.scc.webreg.drools.KnowledgeSessionService");
			
			List<RegistryEntity> registryList = registryService.findByServiceAndStatusAndIDPGood(ssn, registryStatus, 
					new Date(System.currentTimeMillis() - lastUpdate), limit);
			
			if (registryList.size() == 0)
				logger.debug("No LostAccess registries found");
			
			for (RegistryEntity registry : registryList) {
				try {
					logger.info("Deregister Registry {}", registry.getId());
					UserEntity user = registry.getUser();
					if ((System.currentTimeMillis() - user.getLastUpdate().getTime()) > lastUserUpdate) {
						// user is too old, try update first
						logger.info("User {} lastUpdate is older than {}ms. Trying update", user.getEppn(), lastUserUpdate);
						try {
							userService.updateUserFromIdp(user, "lost-access-reg-job");
						} catch (UserUpdateException e) {
							logger.info("Exception while Querying IDP: {}", e.getMessage());
							if (e.getCause() != null) {
								logger.info("Cause is: {}", e.getCause().getMessage());
								if (e.getCause().getCause() != null) {
									logger.info("Inner Cause is: {}", e.getCause().getCause().getMessage());
								}
							}
						}
					}
					List<RegistryEntity> tempRegistryList = new ArrayList<RegistryEntity>();
					tempRegistryList.add(registry);
					knowledgeSessionService.checkRules(tempRegistryList, user, "lost-access-reg-job", false);
					
					if (RegistryStatus.LOST_ACCESS.equals(registry.getRegistryStatus())) {
						registerUserService.deregisterUser(registry, "lost-access-reg-job");
					}
				} catch (RegisterException e) {
					logger.info("Could not deregister", e);
				}
			}
			
		} catch (NamingException e) {
			logger.warn("Could not Deregister LostAccess Registries: {}", e);
		}
	}

}