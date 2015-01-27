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
package edu.kit.scc.webreg.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.util.SessionManager;

@Named("userIndexBean")
@RequestScoped
public class UserIndexBean {

	private List<ServiceEntity> allServiceList;
	private List<ServiceEntity> unregisteredServiceList;
	
	private List<RegistryEntity> userRegistryList;
	private List<RegistryEntity> pendingRegistryList;

	private Map<ServiceEntity, String>	serviceAccessMap; 
	
	private UserEntity user;
	
	@Inject
	private Logger logger;
	
    @Inject
    private ServiceService serviceService;

    @Inject
    private RegistryService registryService;

    @Inject 
    private SessionManager sessionManager;
    
    @Inject
    private UserService userService;

    @Inject
    private GroupService groupService;
    
	@Inject
	private KnowledgeSessionService knowledgeSessionService;

    @PostConstruct
    public void init() {
    	user = userService.findByIdWithStore(sessionManager.getUserId());
    	allServiceList = serviceService.findAllPublishedWithServiceProps();
    	userRegistryList = registryService.findByUserAndStatus(user, RegistryStatus.ACTIVE);
    	userRegistryList.addAll(registryService.findByUserAndStatus(user, RegistryStatus.LOST_ACCESS));
    	pendingRegistryList = registryService.findByUserAndStatus(user, RegistryStatus.PENDING);
    	
    	unregisteredServiceList = new ArrayList<ServiceEntity>(allServiceList);
    	
    	serviceAccessMap = new HashMap<ServiceEntity, String>(userRegistryList.size());

    	
    	for (RegistryEntity registry : userRegistryList) {
    		unregisteredServiceList.remove(registry.getService());
    	}   	

    	for (RegistryEntity registry : pendingRegistryList) {
    		unregisteredServiceList.remove(registry.getService());
    	}   	

    	long start = System.currentTimeMillis();
    	checkServiceAccess(userRegistryList, user);
    	long end = System.currentTimeMillis();
    	logger.debug("Rule processing took {} ms", end - start);

    	List<GroupEntity> groupList = groupService.findByUser(user);
    	String groupString = groupsToString(groupList);
    	
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
	}

    public Boolean isServiceRegistered(ServiceEntity service) {
    	if (service == null)
    		return false;
    	
    	return (! unregisteredServiceList.contains(service));
    }

    public String getServiceAccessStatus(ServiceEntity service) {
    	return serviceAccessMap.get(service);
    }
    
    public List<ServiceEntity> getAllServiceList() {
   		return allServiceList;
    }

    public List<RegistryEntity> getUserRegistryList() {
   		return userRegistryList;
    }

	public UserEntity getUser() {
		return user;
	}

	public List<RegistryEntity> getPendingRegistryList() {
		return pendingRegistryList;
	}

	private void checkServiceAccess(List<RegistryEntity> registryList, UserEntity user) {
		Map<RegistryEntity, List<Object>> objectMap = knowledgeSessionService.checkRules(userRegistryList, user, "user-self", false);
		
		for (Entry<RegistryEntity, List<Object>> entry : objectMap.entrySet()) {
			RegistryEntity registry = entry.getKey();
			List<Object> objectList = entry.getValue();
			
			StringBuffer sb = new StringBuffer();
			for (Object o : objectList) {
				if (o instanceof OverrideAccess) {
					objectList.clear();
					sb.setLength(0);
					logger.debug("Removing requirements due to OverrideAccess");
					break;
				}
				else if (o instanceof UnauthorizedUser) {
					String s = ((UnauthorizedUser) o).getMessage();
					sb.append(s);
				}
			}

			if (sb.length() > 0)
				serviceAccessMap.put(registry.getService(), sb.toString());
		}
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
}
