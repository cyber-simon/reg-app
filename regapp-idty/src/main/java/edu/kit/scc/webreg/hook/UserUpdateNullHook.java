package edu.kit.scc.webreg.hook;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public class UserUpdateNullHook implements UserUpdateHook {

	private static final Logger logger = LoggerFactory.getLogger(UserUpdateNullHook.class);
	
	@Override
	public boolean preUpdateUser(UserEntity user, Map<String, String> genericStore, Map<String, List<Object>> attributeMap, String executor,
			ServiceEntity service, StringBuffer debugLog) {
		logger.info("UserUpdateNullHook preUpdateUser called");
		return false;
	}

	@Override
	public boolean postUpdateUser(UserEntity user, Map<String, String> genericStore, Map<String, List<Object>> attributeMap, String executor,
			ServiceEntity service, StringBuffer debugLog) {
		logger.info("UserUpdateNullHook postUpdateUser called");
		return false;
	}

}
