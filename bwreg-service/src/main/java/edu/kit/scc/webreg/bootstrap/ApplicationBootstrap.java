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

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.regapp.saml.SamlBootstrap;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SerialEntity;
import edu.kit.scc.webreg.hook.HookManager;
import edu.kit.scc.webreg.service.AdminUserService;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.SerialService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.oidc.OidcOpConfigurationService;
import edu.kit.scc.webreg.service.timer.ClusterSchedulerManager;
import edu.kit.scc.webreg.service.timer.StandardScheduler;
import edu.kit.scc.webreg.service.tpl.TemplateUrlStreamHandlerFactory;

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
	private ClusterSchedulerManager clusterSchedulerManager;

	@Inject
	private BpmProcessService bpmProcessService;
	
	@Inject 
	private HookManager hookManager;
	
	@Inject
	private IdentityService identityService;
	
	@Inject
	private OidcOpConfigurationService oidcOpConfigService;
	
	@Inject
	private SamlBootstrap samlBootstrap;
	
	@PostConstruct
	public void init() {
		
		logger.info("Initializing Application Configuration");
		appConfig.init();
		
		logger.info("Register Template URL Stream handler");
		registerUrlHandler();
		
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
    	checkRole("RestAdmin");
    	checkRole("RestServiceAdmin");
    	checkRole("RestGroupAdmin");
    	checkRole("SamlAdmin");
    	checkRole("OidcAdmin");
    	checkRole("BusinessRuleAdmin");
    	checkRole("BulkAdmin");
    	checkRole("TimerAdmin");
    	checkRole("AuditAdmin");
    	checkRole("MailAdmin");
    	checkRole("TokenAdmin");
    	checkRole("User");
    	checkRole("AttributeSourceAdmin");
    	checkRole("ExternalUserAdmin");	
    	
    	if (adminUserService.findByUsername("admin") == null) {
        	logger.info("Initializing admin Account");
    		AdminUserEntity a = adminUserService.createNew();
    		a.setUsername("admin");
    		a.setPassword("secret");
    		Set<RoleEntity> roles = new HashSet<RoleEntity>();
    		roles.add(roleService.findByName("MasterAdmin"));
    		a.setRoles(roles);
    		adminUserService.save(a);
    	}

		logger.info("Initializing Hooks");
    	hookManager.reloadHooks();
		
    	samlBootstrap.init();
        
    	oidcOpConfigService.fixStatus();
    	
        bpmProcessService.init();
        
        identityService.createMissingIdentities();
        
        standardScheduler.initialize();
        
        clusterSchedulerManager.initialize();
        
	}

	private void registerUrlHandler() {
		try {
			final Field factoryField = URL.class.getDeclaredField("factory");
			factoryField.setAccessible(true);
			final Field lockField = URL.class.getDeclaredField("streamHandlerLock");
			lockField.setAccessible(true);

			synchronized (lockField.get(null)) {
				final URLStreamHandlerFactory urlStreamHandlerFactory = (URLStreamHandlerFactory) factoryField.get(null);
				factoryField.set(null, null);
				URL.setURLStreamHandlerFactory(new TemplateUrlStreamHandlerFactory(urlStreamHandlerFactory));
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			logger.warn("Could not register Template URL Stream Handler");
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
