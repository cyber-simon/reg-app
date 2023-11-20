package edu.kit.scc.webreg.service;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.ServletException;

import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface UserLoginService {

	Map<String, String> ecpLogin(String eppn, String serviceShortName,
			String password, String localHostName) throws IOException,
			ServletException, RestInterfaceException;

	Map<String, String> ecpLogin(Long regId, String password,
			String localHostName) throws IOException, ServletException,
			RestInterfaceException;

}
