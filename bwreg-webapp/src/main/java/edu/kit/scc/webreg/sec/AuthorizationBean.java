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
package edu.kit.scc.webreg.sec;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.AuthorizationService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.RoleCache;

@Named("authorizationBean")
@RequestScoped
public class AuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<RegistryEntity> userRegistryList;
	
	@Inject
	private AuthorizationService authService;
	
    @Inject 
    private SessionManager sessionManager;
    
    @Inject
    private ApplicationConfig appConfig;
    
    @Inject
    private RoleCache roleCache;

    @Inject 
    private HttpServletRequest request;
    
    @Inject
    private ServiceDao serviceDao;
    
    @PostConstruct
    private void init() {
    	if (sessionManager.getIdentityId() == null)
    		return;
    	
    	userRegistryList = authService.loadAll(sessionManager, sessionManager.getIdentityId(), request);
	}

    public Duration getLoggedInSince() {
    	if (sessionManager.getLoginTime() != null) {
    		return Duration.between(sessionManager.getLoginTime(), Instant.now());
    	}
    	else {
    		return null;
    	}
    }
    
    public Duration getTwoFaElevatedSince() {
    	if (sessionManager.getTwoFaElevation() != null) {
    		return Duration.between(sessionManager.getTwoFaElevation(), Instant.now());
    	}
    	else {
    		return null;
    	}
    }

    public Boolean isTwoFaElevated() {
		long elevationTime = 5L * 60L * 1000L;
		if (appConfig.getConfigValue("elevation_time") != null) {
			elevationTime = Long.parseLong(appConfig.getConfigValue("elevation_time"));
		}

		if (sessionManager.getTwoFaElevation() != null &&
				(System.currentTimeMillis() - sessionManager.getTwoFaElevation().toEpochMilli()) < elevationTime) {
			return true;
		}
		else {
			return false;
		}
    }
    
    public boolean isUserInRole(String roleName) {
    	if (roleName.startsWith("ROLE_"))
    		roleName = roleName.substring(5);
    	
    	RoleEntity role = roleCache.getIdFromRolename(roleName);
    	
    	if (role == null)
    		return false;
    	
    	return sessionManager.isUserInRole(role);
    }

    public boolean isUserInRoleEntity(RoleEntity role) {
    	if (role == null)
    		return false;
    	
    	return sessionManager.isUserInRole(role);
    }

    public boolean isUserInRoles(Set<RoleEntity> roles) {
    	for (RoleEntity role : roles) {
    		if (isUserInRoleEntity(role))
    			return true;
    	}
    	return false;
    }
    
    public boolean isUserInService(ServiceEntity service) {
    	if (service == null)
    		return false;
    	
    	for (RegistryEntity registry : userRegistryList) {
    		if (registry.getService().getId().equals(service.getId()))
    			return true;
    	}
    	return false;
    }
    
    public boolean isUserServiceAdmin(ServiceEntity service) {
    	if (service == null)
    		return false;
    	return sessionManager.getServiceAdminList().contains(service.getId());
    }

    public boolean isUserServiceApprover(ServiceEntity service) {
    	if (service == null)
    		return false;    	
    	return sessionManager.getServiceApproverList().contains(service.getId());
    }

    public boolean isUserServiceSshPubKeyApprover(ServiceEntity service) {
    	if (service == null)
    		return false;    	
    	return sessionManager.getServiceSshPubKeyApproverList().contains(service.getId());
    }

    public boolean isUserServiceHotline(ServiceEntity service) {
    	if (service == null)
    		return false;
    	return sessionManager.getServiceHotlineList().contains(service.getId());
    }

    public boolean isUserServiceGroupAdmin(ServiceEntity service) {
    	if (service == null)
    		return false;
    	return sessionManager.getServiceGroupAdminList().contains(service.getId());
    }

    public boolean isUserServiceProjectAdmin(ServiceEntity service) {
    	if (service == null)
    		return false;
    	return sessionManager.getServiceProjectAdminList().contains(service.getId());
    }

    public List<RegistryEntity> getUserRegistryList() {
    	if (userRegistryList == null) init();
   		return userRegistryList;
    }

	public List<ServiceEntity> getServiceApproverList() {
		return serviceDao.fetchAll(sessionManager.getServiceApproverList());
	}

	public List<ServiceEntity> getServiceSshPubKeyApproverList() {
		return serviceDao.fetchAll(sessionManager.getServiceSshPubKeyApproverList());
	}

	public List<ServiceEntity> getServiceAdminList() {
		return serviceDao.fetchAll(sessionManager.getServiceAdminList());
	}

	public List<ServiceEntity> getServiceHotlineList() {
		return serviceDao.fetchAll(sessionManager.getServiceHotlineList());
	}

	public List<ServiceEntity> getServiceGroupAdminList() {
		return serviceDao.fetchAll(sessionManager.getServiceGroupAdminList());
	}

	public List<ServiceEntity> getServiceProjectAdminList() {
		return serviceDao.fetchAll(sessionManager.getServiceProjectAdminList());
	}

	public boolean isPasswordCapable(ServiceEntity serviceEntity) {
		if (serviceEntity.getPasswordCapable() != null)
			return serviceEntity.getPasswordCapable();
		else
			return false;
	}

	public boolean isSshPubKeyCapable(ServiceEntity serviceEntity) {
		if (serviceEntity.getSshPubKeyCapable() != null)
			return serviceEntity.getSshPubKeyCapable();
		else
			return false;
	}

	public List<ServiceEntity> getUnregisteredServiceList() {
		return serviceDao.fetchAll(sessionManager.getUnregisteredServiceList());
	}

	public ApplicationConfig getAppConfig() {
		return appConfig;
	}
}
