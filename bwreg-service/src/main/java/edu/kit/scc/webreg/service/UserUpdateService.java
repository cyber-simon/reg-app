package edu.kit.scc.webreg.service;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;

import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface UserUpdateService {

	Map<String, String> updateUser(String eppn, String serviceShortName,
			String localHostName) throws IOException, ServletException,
			RestInterfaceException;

	Map<String, String> updateUser(Long regId, String localHostName)
			throws IOException, ServletException, RestInterfaceException;


}
