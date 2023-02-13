package edu.kit.scc.webreg.service;

import java.util.Map;

import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface UserUpdateService {

	Map<String, String> updateUser(String eppn, String serviceShortName, String localHostName, String executor)
			throws RestInterfaceException;

	Map<String, String> updateUser(Long regId, String localHostName, String executor) throws RestInterfaceException;

	Map<String, String> updateUser(String eppn, String localHostName, String executor) throws RestInterfaceException;

	void updateUserAsync(String eppn, String localHostName, String executor);

	Map<String, String> updateUser(Integer uidNumber, String serviceShortName, String localHostName, String executor)
			throws RestInterfaceException;

	Map<String, String> updateUserByGenericStore(String key, String value, String serviceShortName,
			String localHostName, String executor) throws RestInterfaceException;

	Map<String, String> updateUserByAttributeStore(String key, String value, String serviceShortName,
			String localHostName, String executor) throws RestInterfaceException;

}
