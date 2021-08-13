package edu.kit.scc.webreg.hook;

import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface UserUpdateHook {

	boolean preUpdateUser(UserEntity user, Map<String, String> genericStore, Map<String, List<Object>> attributeMap, String executor, 
			ServiceEntity service, StringBuffer debugLog) throws UserUpdateHookException; 

	boolean postUpdateUser(UserEntity user, Map<String, String> genericStore, Map<String, List<Object>> attributeMap, String executor, 
			ServiceEntity service, StringBuffer debugLog) throws UserUpdateHookException; 
	
}
