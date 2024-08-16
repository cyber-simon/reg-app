package edu.kit.scc.webreg.service.impl;

import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;

public interface UserUpdater<T extends UserEntity> {

	public T updateUser(T user, Map<String, List<Object>> attributeMap, String executor, StringBuffer debugLog, String lastLoginHost)
			throws UserUpdateException;

	public T updateUser(T user, Map<String, List<Object>> attributeMap, String executor, ServiceEntity service, StringBuffer debugLog, String lastLoginHost)
			throws UserUpdateException;

	public T updateUserFromHomeOrg(T user, ServiceEntity service, String executor,
			StringBuffer debugLog) throws UserUpdateException;

	public T expireUser(T user, String executor);
}
