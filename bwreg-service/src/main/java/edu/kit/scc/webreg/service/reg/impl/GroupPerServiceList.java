package edu.kit.scc.webreg.service.reg.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;

public class GroupPerServiceList {

	private Map<ServiceEntity, Set<ServiceGroupFlagEntity>> serviceMap;
	
	public GroupPerServiceList() {
		serviceMap = new HashMap<ServiceEntity, Set<ServiceGroupFlagEntity>>();
	}
	
	public void addGroupToUpdate(ServiceGroupFlagEntity groupFlag) {
		if (! serviceMap.containsKey(groupFlag.getService())) {
			serviceMap.put(groupFlag.getService(), new HashSet<ServiceGroupFlagEntity>());
		}
		
		serviceMap.get(groupFlag.getService()).add(groupFlag);
	}
	
	public Set<ServiceEntity> getServices() {
		return serviceMap.keySet();
	}
	
	public Set<ServiceGroupFlagEntity> getGroupFlagsForService(ServiceEntity service) {
		return serviceMap.get(service);
	}	
}
