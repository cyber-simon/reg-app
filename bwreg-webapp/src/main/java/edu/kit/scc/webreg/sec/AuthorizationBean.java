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

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
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

    @PostConstruct
    private void init() {
    	if (sessionManager.getIdentityId() == null)
    		return;
    	
    	userRegistryList = authService.loadAll(sessionManager, sessionManager.getIdentityId());
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
    
    public boolean isUserServiceAdmin(ServiceEntity id) {
    	if (id == null)
    		return false;
    	return sessionManager.getServiceAdminList().contains(id);
    }

    public boolean isUserServiceApprover(ServiceEntity id) {
    	if (id == null)
    		return false;    	
    	return sessionManager.getServiceApproverList().contains(id);
    }

    public boolean isUserServiceSshPubKeyApprover(ServiceEntity id) {
    	if (id == null)
    		return false;    	
    	return sessionManager.getServiceSshPubKeyApproverList().contains(id);
    }

    public boolean isUserServiceHotline(ServiceEntity id) {
    	if (id == null)
    		return false;
    	return sessionManager.getServiceHotlineList().contains(id);
    }

    public boolean isUserServiceGroupAdmin(ServiceEntity id) {
    	if (id == null)
    		return false;
    	return sessionManager.getServiceGroupAdminList().contains(id);
    }

    public List<RegistryEntity> getUserRegistryList() {
    	if (userRegistryList == null) init();
   		return userRegistryList;
    }

	public List<ServiceEntity> getServiceApproverList() {
		return sessionManager.getServiceApproverList();
	}

	public List<ServiceEntity> getServiceSshPubKeyApproverList() {
		return sessionManager.getServiceSshPubKeyApproverList();
	}

	public List<ServiceEntity> getServiceAdminList() {
		return sessionManager.getServiceAdminList();
	}

	public List<ServiceEntity> getServiceHotlineList() {
		return sessionManager.getServiceHotlineList();
	}

	public List<ServiceEntity> getServiceGroupAdminList() {
		return sessionManager.getServiceGroupAdminList();
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
		return sessionManager.getUnregisteredServiceList();
	}

	public ApplicationConfig getAppConfig() {
		return appConfig;
	}
}
