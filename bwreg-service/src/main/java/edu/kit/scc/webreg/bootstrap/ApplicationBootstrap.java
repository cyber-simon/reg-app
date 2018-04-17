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
package edu.kit.scc.webreg.bootstrap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.slf4j.Logger;

import edu.kit.scc.webreg.drools.BpmProcessService;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SerialEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.AdminUserService;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.SerialService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.impl.HookManager;
import edu.kit.scc.webreg.service.mail.TemplateRenderer;
import edu.kit.scc.webreg.service.timer.StandardScheduler;

@Singleton
@Startup
public class ApplicationBootstrap {

	@Inject
	private Logger logger;
	
	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private GroupService groupService;

	@Inject
	private UserService userService;
	
	@Inject
	private RoleService roleService;
	
	@Inject
	private ServiceService serviceService;
	
	@Inject
	private AdminUserService adminUserService;
	
	@Inject
	private SerialService serialService;
	
	@Inject
	private StandardScheduler standardScheduler;

	@Inject
	private BpmProcessService bpmProcessService;
	
	@Inject
	private TemplateRenderer velocityRenderer;
	
	@Inject 
	private HookManager hookManager;
	
	@PostConstruct
	public void init() {
		
		logger.info("Initializing Application Configuration");
		appConfig.init();
		
		logger.info("Initializing Serials");
		checkSerial("uid-number-serial", 900000L);
		checkSerial("gid-number-serial", 500000L);

		logger.info("Initializing Groups");
		checkGroup("invalid", 499999);
		
		logger.info("Initializing standard Roles");
    	checkRole("MasterAdmin");
    	checkRole("RoleAdmin");
    	checkRole("UserAdmin");
    	checkRole("GroupAdmin");
    	checkRole("ServiceAdmin");
    	checkRole("RestServiceAdmin");
    	checkRole("SamlAdmin");
    	checkRole("BusinessRuleAdmin");
    	checkRole("BulkAdmin");
    	checkRole("TimerAdmin");
    	checkRole("AuditAdmin");
    	checkRole("User");
    	checkRole("AttributeSourceAdmin");
    	
    	logger.info("Initializing admin Account");
    	if (adminUserService.findByUsername("admin") == null) {
    		AdminUserEntity a = adminUserService.createNew();
    		a.setUsername("admin");
    		a.setPassword("secret");
    		Set<RoleEntity> roles = new HashSet<RoleEntity>();
    		roles.add(roleService.findByName("MasterAdmin"));
    		a.setRoles(roles);
    		adminUserService.save(a);
    	}

    	logger.info("Setting PasswordCapable and GroupCapable on all services, according to implemented interfaces");
    	List<ServiceEntity> serviceList = serviceService.findAll();
    	for (ServiceEntity service : serviceList) {
        	logger.debug("Update capabilities on service {}", service.getName());
    		serviceService.updateCapabilities(service);
    	}
    	
		logger.info("Initializing Hooks");
    	hookManager.reloadHooks();
		
    	try {
    		logger.info("OpenSAML Bootstrap...");
			InitializationService.initialize();
				        
		} catch (InitializationException e) {
			logger.error("Serious Error happened", e);
		}
        
        bpmProcessService.init();
        
        velocityRenderer.init();
        
        standardScheduler.initialize();
	}
	
    private void checkGroup(String name, Integer createActual) {
    	GroupEntity entity = groupService.findByName(name);
    	if (entity == null) {
    		entity = groupService.createNew();
    		entity.setName(name);
    		entity.setGidNumber(createActual);
    		groupService.save(entity);
    	}    	
    }

    private void checkRole(String roleName) {
    	if (roleService.findByName(roleName) == null) {
    		RoleEntity role = roleService.createNew();
    		role.setName(roleName);
    		roleService.save(role);
    	}    	
    }

    private void checkSerial(String serialName, Long createActual) {
    	SerialEntity serial = serialService.findByName(serialName);
    	if (serial == null) {
    		serial = serialService.createNew();
    		serial.setName(serialName);
    		serial.setActual(createActual);
    		serialService.save(serial);
    	}    	
    }

}
