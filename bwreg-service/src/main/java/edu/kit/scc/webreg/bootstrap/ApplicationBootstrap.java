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

import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLConfigurator;
import org.slf4j.Logger;

import edu.kit.scc.webreg.drools.BpmProcessService;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SerialEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.AdminUserService;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.GroupServiceHook;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.SerialService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.UserServiceHook;
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
    	checkRole("SamlAdmin");
    	checkRole("BusinessRuleAdmin");
    	checkRole("BulkAdmin");
    	checkRole("TimerAdmin");
    	checkRole("AuditAdmin");
    	checkRole("User");
    	
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
    	addUserHooks();
    	addGroupHooks();
		
    	userService.convertLegacyUsers();

        try {
			
        	logger.info("OpenSAML Bootstrap...");
        	DefaultBootstrap.bootstrap();

        	logger.info("Loading XMLTooling configuration /liberty-paos-config.xml");
	        XMLConfigurator configurator = new XMLConfigurator();
	        configurator.load(Configuration.class.getResourceAsStream("/liberty-paos-config.xml"));
			
		} catch (ConfigurationException e) {
			logger.error("Serious Error happened", e);
		}

        
        bpmProcessService.init();
        
        velocityRenderer.init();
        
        standardScheduler.initialize();
	}
	
	private void addUserHooks() {
		String hooksString = appConfig.getConfigValue("user_hooks");
		if (hooksString != null && hooksString.length() > 0) {
			hooksString = hooksString.trim();
			String[] hooks = hooksString.split(";");
			for (String hook : hooks) {
				hook = hook.trim();
				try {
					UserServiceHook h = (UserServiceHook) Class.forName(hook).newInstance();
					h.setAppConfig(appConfig);
					hookManager.addUserHook(h);
				} catch (InstantiationException e) {
					logger.warn("Could not spawn hook " + hook, e);
				} catch (IllegalAccessException e) {
					logger.warn("Could not spawn hook " + hook, e);
				} catch (ClassNotFoundException e) {
					logger.warn("Could not spawn hook " + hook, e);
				}
			}
		}		
	}
	
	private void addGroupHooks() {
		String hooksString = appConfig.getConfigValue("group_hooks");
		if (hooksString != null && hooksString.length() > 0) {
			hooksString = hooksString.trim();
			String[] hooks = hooksString.split(";");
			for (String hook : hooks) {
				hook = hook.trim();
				try {
					GroupServiceHook h = (GroupServiceHook) Class.forName(hook).newInstance();
					h.setAppConfig(appConfig);
					hookManager.addGroupHook(h);
				} catch (InstantiationException e) {
					logger.warn("Could not spawn hook " + hook, e);
				} catch (IllegalAccessException e) {
					logger.warn("Could not spawn hook " + hook, e);
				} catch (ClassNotFoundException e) {
					logger.warn("Could not spawn hook " + hook, e);
				}
			}
		}		
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
