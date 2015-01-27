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
import edu.kit.scc.webreg.util.SessionManager;

@Named("authorizationBean")
@RequestScoped
public class AuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ServiceEntity> unregisteredServiceList;
	private List<RegistryEntity> userRegistryList;
	private List<RegistryEntity> pendingRegistryList;
	private List<ServiceEntity> serviceApproverList;
	private List<ServiceEntity> serviceAdminList;
	private List<ServiceEntity> serviceHotlineList;
	private List<ServiceEntity> serviceGroupAdminList;
	private List<RoleEntity> roleList;
	
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
    
    @PostConstruct
    private void init() {
    	if (sessionManager.getUserId() == null)
    		return;
    	
    	UserEntity user = userService.findByIdWithStore(sessionManager.getUserId());
    	List<GroupEntity> groupList = groupService.findByUser(user);
    	String groupString = groupsToString(groupList);
    	
    	userRegistryList = registryService.findByUserAndNotStatus(user, RegistryStatus.DELETED);

    	serviceApproverList = new ArrayList<ServiceEntity>();
    	serviceAdminList = new ArrayList<ServiceEntity>();
    	serviceHotlineList = new ArrayList<ServiceEntity>();
    	serviceGroupAdminList = new ArrayList<ServiceEntity>();
    	
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
    					(! groupString.matches(groupFilter)))
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
    	
    	roleList = roleService.findByUser(user);
    	
    	for (RoleEntity role : roleList) {
    		if (role instanceof AdminRoleEntity) {
    			serviceAdminList.addAll(serviceService.findByAdminRole(role));
    			serviceHotlineList.addAll(serviceService.findByHotlineRole(role));
    		}
    		else if (role instanceof ApproverRoleEntity) {
        		serviceApproverList.addAll(serviceService.findByApproverRole(role));
    		}
    		else if (role instanceof GroupAdminRoleEntity) {
    			serviceGroupAdminList.addAll(serviceService.findByGroupAdminRole(role));
    		}
    	}
	}

    public boolean isUserInRole(RoleEntity role) {
    	if (role == null)
    		return false;
    	
    	for (RoleEntity r : roleList) {
    		if (role.equals(r))
    			return true;
    	}
    	
    	return false;
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
    	
    	for (ServiceEntity service : getServiceAdminList()) {
    		if (id.equals(service.getId()))
    			return true;
    	}
    	return false;
    }

    public boolean isUserServiceHotline(Long id) {
    	if (id == null)
    		return false;
    	
    	for (ServiceEntity service : getServiceHotlineList()) {
    		if (id.equals(service.getId()))
    			return true;
    	}
    	return false;
    }

    public boolean isUserServiceGroupAdmin(Long id) {
    	if (id == null)
    		return false;
    	
    	for (ServiceEntity service : getServiceGroupAdminList()) {
    		if (id.equals(service.getId()))
    			return true;
    	}
    	return false;
    }
    
    public List<RegistryEntity> getUserRegistryList() {
    	if (userRegistryList == null) init();
   		return userRegistryList;
    }

	public List<ServiceEntity> getServiceApproverList() {
    	if (serviceApproverList == null) init();
		return serviceApproverList;
	}

	public List<ServiceEntity> getServiceAdminList() {
    	if (serviceAdminList == null) init();
		return serviceAdminList;
	}

	public List<ServiceEntity> getServiceHotlineList() {
    	if (serviceHotlineList == null) init();
		return serviceHotlineList;
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

	private String groupsToString(List<GroupEntity> groupList) {
		StringBuilder sb = new StringBuilder();
		for (GroupEntity group : groupList) {
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

	public List<ServiceEntity> getServiceGroupAdminList() {
		return serviceGroupAdminList;
	}
}
