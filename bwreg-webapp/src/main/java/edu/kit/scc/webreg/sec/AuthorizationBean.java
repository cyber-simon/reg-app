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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
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
import edu.kit.scc.webreg.util.RoleCache;
import edu.kit.scc.webreg.util.SessionManager;

@Named("authorizationBean")
@RequestScoped
public class AuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ServiceEntity> unregisteredServiceList;
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
    
    @PostConstruct
    private void init() {
    	if (sessionManager.getUserId() == null)
    		return;
    	
    	long start, end;
    	
    	start = System.currentTimeMillis();
    	UserEntity user = userService.findByIdWithStore(sessionManager.getUserId());
    	end = System.currentTimeMillis();
    	logger.debug("user find by id with store loading took {} ms", (end-start));

    	if (sessionManager.getRoleSetCreated() == null || 
    			(System.currentTimeMillis() - sessionManager.getRoleSetCreated()) > 5 * 60 * 1000L) {
	    	start = System.currentTimeMillis();
	    	Set<GroupEntity> groupList = groupService.findByUserWithParents(user);
	    	
	    	sessionManager.setGroupString(groupsToString(groupList));
	    	
	    	for (GroupEntity g : groupList) {
	    		sessionManager.getGroupList().add(g.getId());
	    	}
	    	
	    	end = System.currentTimeMillis();
	    	logger.debug("groups loading took {} ms", (end-start));
    	}
    	
    	start = System.currentTimeMillis();
    	userRegistryList = registryService.findByUserAndNotStatus(user, RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
    	end = System.currentTimeMillis();
    	logger.debug("registered servs loading took {} ms", (end-start));

    	unregisteredServiceList = serviceService.findAllPublishedWithServiceProps();
    	
    	for (RegistryEntity registry : userRegistryList) {
    		unregisteredServiceList.remove(registry.getService());
    	}

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
    					(! sessionManager.getGroupString().matches(groupFilter)))
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
    	
    	if (sessionManager.getRoleSetCreated() == null || 
    			(System.currentTimeMillis() - sessionManager.getRoleSetCreated()) > 5 * 60 * 1000L) {
	    	start = System.currentTimeMillis();
	
	    	List<RoleEntity> roleList = roleService.findByUser(user);
	    	
	    	for (RoleEntity role : roleList) {
	    		sessionManager.addRole(role.getId());
	    		if (role instanceof AdminRoleEntity) {
	    			for (ServiceEntity s : serviceService.findByAdminRole(role))
	    				sessionManager.getServiceAdminList().add(s.getId());
	    			for (ServiceEntity s : serviceService.findByHotlineRole(role))
	    				sessionManager.getServiceHotlineList().add(s.getId());
	    		}
	    		else if (role instanceof ApproverRoleEntity) {
	    			for (ServiceEntity s : serviceService.findByApproverRole(role))
	    				sessionManager.getServiceApproverList().add(s.getId());
	    		}
	    		else if (role instanceof GroupAdminRoleEntity) {
	    			for (ServiceEntity s : serviceService.findByGroupAdminRole(role))
	    				sessionManager.getServiceGroupAdminList().add(s.getId());
	    		}
	    	}
	    	end = System.currentTimeMillis();
	    	logger.debug("Role loading took {} ms", (end-start));
	    	
	    	sessionManager.setRoleSetCreated(System.currentTimeMillis());
    	}
	}

    public boolean isUserInRole(String roleName) {
    	if (roleName.startsWith("ROLE_"))
    		roleName = roleName.substring(5);
    	
    	Long roleId = roleCache.getIdFromRolename(roleName);
    	
    	if (roleId == null)
    		return false;
    	
    	return sessionManager.isUserInRole(roleId);
    }

    public boolean isUserInRole(RoleEntity role) {
    	if (role == null)
    		return false;
    	
    	return sessionManager.isUserInRole(role.getId());
    }

    public boolean isUserInRoles(Set<RoleEntity> roles) {
    	for (RoleEntity role : roles) {
    		if (isUserInRole(role))
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
    
    public boolean isUserServiceAdmin(Long id) {
    	if (id == null)
    		return false;
    	return sessionManager.getServiceAdminList().contains(id);
    }

    public boolean isUserServiceApprover(Long id) {
    	if (id == null)
    		return false;    	
    	return sessionManager.getServiceApproverList().contains(id);
    }

    public boolean isUserServiceHotline(Long id) {
    	if (id == null)
    		return false;
    	return sessionManager.getServiceHotlineList().contains(id);
    }

    public boolean isUserServiceGroupAdmin(Long id) {
    	if (id == null)
    		return false;
    	return sessionManager.getServiceGroupAdminList().contains(id);
    }
    
    public List<RegistryEntity> getUserRegistryList() {
    	if (userRegistryList == null) init();
   		return userRegistryList;
    }

	public List<Long> getServiceApproverList() {
		return sessionManager.getServiceApproverList();
	}

	public List<Long> getServiceAdminList() {
		return sessionManager.getServiceAdminList();
	}

	public List<Long> getServiceHotlineList() {
		return sessionManager.getServiceHotlineList();
	}

	public List<Long> getServiceGroupAdminList() {
		return sessionManager.getServiceGroupAdminList();
	}

	public boolean isPasswordCapable(ServiceEntity serviceEntity) {
		if (serviceEntity.getPasswordCapable() != null)
			return serviceEntity.getPasswordCapable();
		else
			return false;
	}

	public List<ServiceEntity> getUnregisteredServiceList() {
		return unregisteredServiceList;
	}

	private String groupsToString(Set<GroupEntity> groups) {
		StringBuilder sb = new StringBuilder();
		for (GroupEntity group : groups) {
			if (group instanceof HomeOrgGroupEntity &&  
					((HomeOrgGroupEntity) group).getPrefix() != null) {
				sb.append(((HomeOrgGroupEntity) group).getPrefix());
			}
			sb.append("_");
			sb.append(group.getName());
			sb.append(";");
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);
		
		return sb.toString();
	}

	public ApplicationConfig getAppConfig() {
		return appConfig;
	}
}
