package edu.kit.scc.webreg.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;

public abstract class AbstractUserUpdater<T extends UserEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract T updateUser(T user, Map<String, List<Object>> attributeMap, String executor, StringBuffer debugLog)
			throws UserUpdateException;

	public abstract T updateUser(T user, Map<String, List<Object>> attributeMap, String executor, ServiceEntity service, StringBuffer debugLog)
			throws UserUpdateException;

	protected void preUpdateUser(UserEntity user, Map<String, List<Object>> attributeMap, String executor, ServiceEntity service, StringBuffer debugLog)
			throws UserUpdateException {
		
	}

	protected void postUpdateUser(UserEntity user, Map<String, List<Object>> attributeMap, String executor, ServiceEntity service, StringBuffer debugLog)
			throws UserUpdateException {
		
	}

}
