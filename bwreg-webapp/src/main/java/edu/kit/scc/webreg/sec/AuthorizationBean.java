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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.GroupAdminRoleEntity;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.RoleCache;

@Named("authorizationBean")
@RequestScoped
public class AuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<RegistryEntity> userRegistryList;
	
	@Inject
	private Logger logger;
	
    @Inject
    private RegistryService registryService;

    @Inject
    private RoleService roleService;

    @Inject
    private ServiceService serviceService;
    
    @Inject 
    private SessionManager sessionManager;
    
    @Inject
    private UserService userService;
    
    @Inject
    private GroupService groupService;
    
    @Inject
    private ApplicationConfig appConfig;
    
    @Inject
    private RoleCache roleCache;
    
    @Inject
    private KnowledgeSessionService knowledgeSessionService;
    
    @PostConstruct
    private void init() {
    	if (sessionManager.getUserId() == null)
    		return;
    	
    	Long rolesTimeout;
    	if (appConfig.getConfigValue("AuthorizationBean_rolesTimeout") != null)
    		rolesTimeout = Long.parseLong(appConfig.getConfigValue("AuthorizationBean_rolesTimeout"));
    	else 
    		rolesTimeout = 1 * 60 * 1000L;

    	Long groupsTimeout;
    	if (appConfig.getConfigValue("AuthorizationBean_groupsTimeout") != null)
    		groupsTimeout = Long.parseLong(appConfig.getConfigValue("AuthorizationBean_groupsTimeout"));
    	else 
    		groupsTimeout = 1 * 60 * 1000L;
    	
    	Long unregisteredServiceTimeout;
    	if (appConfig.getConfigValue("AuthorizationBean_unregisteredServiceTimeout") != null)
    		unregisteredServiceTimeout = Long.parseLong(appConfig.getConfigValue("AuthorizationBean_unregisteredServiceTimeout"));
    	else 
    		unregisteredServiceTimeout = 1 * 60 * 1000L;
    	
    	long start, end;
    	
    	start = System.currentTimeMillis();
    	UserEntity user = userService.findByIdWithStore(sessionManager.getUserId());
    	end = System.currentTimeMillis();
    	logger.trace("user find by id with store loading took {} ms", (end-start));

    	if (sessionManager.getGroupSetCreated() == null || 
    			(System.currentTimeMillis() - sessionManager.getGroupSetCreated()) > groupsTimeout) {
	    	start = System.currentTimeMillis();
	    	Set<GroupEntity> groupList = groupService.findByUserWithChildren(user);
	    	
	    	sessionManager.clearGroups();
    		sessionManager.getGroups().addAll(groupList);
    		for (GroupEntity g : groupList) {
    			sessionManager.getGroupNames().add(g.getName());
    		}
	    	sessionManager.setGroupSetCreated(System.currentTimeMillis());

	    	end = System.currentTimeMillis();
	    	logger.trace("groups loading took {} ms", (end-start));
    	}

    	if (sessionManager.getRoleSetCreated() == null || 
    			(System.currentTimeMillis() - sessionManager.getRoleSetCreated()) > rolesTimeout) {
	    	start = System.currentTimeMillis();

	    	sessionManager.clearRoleList();
	    	
	    	Set<RoleEntity> roles = new HashSet<RoleEntity>(roleService.findByUser(user));
	    	List<RoleEntity> rolesForGroupList = roleService.findByGroups(sessionManager.getGroups());
	    	roles.addAll(rolesForGroupList);

	    	for (RoleEntity role : roles) {
	    		sessionManager.addRole(role);
	    		if (role instanceof AdminRoleEntity) {
	    			for (ServiceEntity s : serviceService.findByAdminRole(role))
	    				sessionManager.getServiceAdminList().add(s);
	    			for (ServiceEntity s : serviceService.findByHotlineRole(role))
	    				sessionManager.getServiceHotlineList().add(s);
	    		}
	    		else if (role instanceof ApproverRoleEntity) {
	    			for (ServiceEntity s : serviceService.findByApproverRole(role))
	    				sessionManager.getServiceApproverList().add(s);
	    		}
	    		else if (role instanceof GroupAdminRoleEntity) {
	    			for (ServiceEntity s : serviceService.findByGroupAdminRole(role))
	    				sessionManager.getServiceGroupAdminList().add(s);
	    		}
	    	}
	    	end = System.currentTimeMillis();
	    	logger.trace("Role loading took {} ms", (end-start));
	    	
	    	sessionManager.setRoleSetCreated(System.currentTimeMillis());
    	}
    	
    	start = System.currentTimeMillis();
    	userRegistryList = registryService.findByUserAndNotStatusAndNotHidden(
    			user, RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
    	end = System.currentTimeMillis();
    	logger.trace("registered servs loading took {} ms", (end-start));
    	
    	if (sessionManager.getUnregisteredServiceCreated() == null || 
    			(System.currentTimeMillis() - sessionManager.getUnregisteredServiceCreated()) > unregisteredServiceTimeout) {
    		
	    	List<ServiceEntity> unregisteredServiceList = serviceService.findAllPublishedWithServiceProps();
	    	
	    	for (RegistryEntity registry : userRegistryList) {
	    		unregisteredServiceList.remove(registry.getService());
	    	}
	
	    	if (appConfig.getConfigValue("service_filter_rule") != null) {
	    		String serviceFilterRule = appConfig.getConfigValue("service_filter_rule");
				logger.debug("Checking service filter rule {}", serviceFilterRule);
		    	start = System.currentTimeMillis();
	
		    	unregisteredServiceList = knowledgeSessionService.checkServiceFilterRule(
		    			serviceFilterRule, user, unregisteredServiceList,
		    			sessionManager.getGroups(), sessionManager.getRoles());
				
		    	end = System.currentTimeMillis();
		    	logger.debug("Rule processing took {} ms", end - start);
	
	    	}
	    	else {
		    	List<ServiceEntity> serviceToRemove = new ArrayList<ServiceEntity>();
		    	for (ServiceEntity s : unregisteredServiceList) {
		    		Map<String, String> serviceProps = s.getServiceProps();
		
		    		if (serviceProps.containsKey("idp_filter")) {
		    			String idpFilter = serviceProps.get("idp_filter");
		    			if (idpFilter != null &&
		    					(! idpFilter.contains(user.getIdp().getEntityId())))
		    				serviceToRemove.add(s);
		    		}
		
		    		if (s.getServiceProps().containsKey("group_filter")) {
		    			String groupFilter = serviceProps.get("group_filter");
		    			if (groupFilter != null &&
		    					(! sessionManager.getGroupNames().contains(groupFilter)))
		    				serviceToRemove.add(s);
		    		}
		
		    		if (s.getServiceProps().containsKey("entitlement_filter")) {
		    			String entitlementFilter = serviceProps.get("entitlement_filter");
		    			String entitlement = user.getAttributeStore().get("urn:oid:1.3.6.1.4.1.5923.1.1.1.7");
		    			if (entitlementFilter != null && entitlement != null &&
		    					(! entitlement.matches(entitlementFilter)))
		    				serviceToRemove.add(s);
		    		}
		    	}
		    	unregisteredServiceList.removeAll(serviceToRemove);
	    	}
	    	
	    	sessionManager.setUnregisteredServiceList(unregisteredServiceList);
	    	sessionManager.setUnregisteredServiceCreated(System.currentTimeMillis());
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

	public List<ServiceEntity> getUnregisteredServiceList() {
		return sessionManager.getUnregisteredServiceList();
	}

	public ApplicationConfig getAppConfig() {
		return appConfig;
	}
}
