package edu.kit.scc.webreg.service;

import java.io.IOException;
import java.util.Map;

import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface UserUpdateService {

	Map<String, String> updateUser(String eppn, String serviceShortName,
			String localHostName, String executor) throws IOException, 
			RestInterfaceException;

	Map<String, String> updateUser(Long regId, String localHostName, String executor)
			throws IOException, RestInterfaceException;

	Map<String, String> updateUser(String eppn, String localHostName, String executor)
			throws IOException, RestInterfaceException;

	void updateUserAsync(String eppn, String localHostName, String executor);


}
