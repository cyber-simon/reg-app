package edu.kit.scc.webreg.service.reg.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public class GroupUpdateStructure {

	private Map<ServiceGroupFlagEntity, Set<UserEntity>> updateMap;
	
	public GroupUpdateStructure() {
		updateMap = new HashMap<ServiceGroupFlagEntity, Set<UserEntity>>();
	}
	
	public void addGroup(ServiceGroupFlagEntity sgf, Set<UserEntity> users) {
		updateMap.put(sgf, users);
	}
	
	public Set<ServiceGroupFlagEntity> getGroupFlags() {
		return updateMap.keySet();
	}
	
	public Set<UserEntity> getUsersForGroupFlag(ServiceGroupFlagEntity sgf) {
		return updateMap.get(sgf);
	}
}
