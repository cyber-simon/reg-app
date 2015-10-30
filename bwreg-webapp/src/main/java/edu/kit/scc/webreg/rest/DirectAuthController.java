/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.TextPropertyDao;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.TextPropertyEntity;
import edu.kit.scc.webreg.exc.GenericRestInterfaceException;
import edu.kit.scc.webreg.exc.LoginFailedException;
import edu.kit.scc.webreg.exc.NoEcpSupportException;
import edu.kit.scc.webreg.exc.NoHostnameConfiguredException;
import edu.kit.scc.webreg.exc.NoIdpForScopeException;
import edu.kit.scc.webreg.exc.NoIdpFoundException;
import edu.kit.scc.webreg.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.exc.NoServiceFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UnauthorizedException;
import edu.kit.scc.webreg.exc.UserUpdateFailedException;
import edu.kit.scc.webreg.rest.dto.ECPResponse;
import edu.kit.scc.webreg.rest.dto.RestError;
import edu.kit.scc.webreg.sec.SecurityFilter;
import edu.kit.scc.webreg.service.UserLoginService;

@Path("/direct-auth")
public class DirectAuthController {

	@Inject
	private Logger logger;
	
	@Inject
	private UserLoginService userLoginService;
	
	@Inject
	private TextPropertyDao textPropertyDao;
	
	@Path("/eppn/{service}")
	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> ecpLogin(@PathParam("service") String serviceShortName,
			@Context HttpServletRequest request)
			throws IOException, ServletException, RestInterfaceException {
		//appConfig.getConfigValue(key)
		
		String eppn = (String) request.getAttribute(SecurityFilter.DIRECT_USER_ID);
		String password = (String) request.getAttribute(SecurityFilter.DIRECT_USER_PW);
		return userLoginService.ecpLogin(eppn, serviceShortName, password, request.getLocalName());
	}

	@Path("/eppn-xml/{service}")
	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	public ECPResponse ecpLoginXml(@PathParam("service") String serviceShortName,
			@Context HttpServletRequest request)
			throws IOException, ServletException, RestInterfaceException {

		ECPResponse response = new ECPResponse();

		try {
			String eppn = (String) request.getAttribute(SecurityFilter.DIRECT_USER_ID);
			String password = (String) request.getAttribute(SecurityFilter.DIRECT_USER_PW);

			userLoginService.ecpLogin(eppn, serviceShortName, password, request.getLocalName());
		}
		catch (NoUserFoundException e) {
			generateFailXml(response, 400, "ecp login failed", "user-not-found");
			return response;
		}
		catch (NoServiceFoundException e) {
			generateFailXml(response, 400, "ecp login failed", "no-such-service");
			return response;
		}
		catch (NoRegistryFoundException e) {
			generateFailXml(response, 400, "ecp login failed", "user-not-registered");
			return response;
		}
		catch (NoIdpForScopeException e) {
			generateFailXml(response, 400, "ecp login failed", "no-idp-for-scope");
			return response;
		}
		catch (NoIdpFoundException e) {
			generateFailXml(response, 500, "ecp login failed", "idp-metadata-error");
			return response;
		}
		catch (NoEcpSupportException e) {
			generateFailXml(response, 400, "ecp login failed", "idp-ecp-not-supported");
			return response;
		}
		catch (NoHostnameConfiguredException e) {
			generateFailXml(response, 500, "ecp login failed", "no-hostname-configured");
			return response;
		}
		catch (LoginFailedException e) {
			generateFailXml(response, 500, "ecp login failed", "login-failed");
			return response;
		}
		catch (UserUpdateFailedException e) {
			generateFailXml(response, 400, "ecp login failed", "user-update-failed");
			return response;
		}
		catch (UnauthorizedException e) {
			response.setCode(405);
			response.setMessage("rules failed");
			
			for (UnauthorizedUser uu : e.getUnauthList()) {
				addXmlError(response, uu.getMessage(), resolveString(uu.getMessage()));
			}
		}
		catch (GenericRestInterfaceException e) {
			generateFailXml(response, 500, "ecp login failed", "generic-error");
			return response;
		}
		catch (RestInterfaceException e) {
			logger.warn("Unmapped RestInterfaceException!", e);
			generateFailXml(response, 400, "ecp login failed", "unknown-error");
			return response;
		}	
		
		response.setCode(200);
		response.setMessage("success");

		return response;
	}

	private void generateFailXml(ECPResponse response, int code, String message, String error) {
		response.setCode(code);
		response.setMessage(message);
		addXmlError(response, error, resolveString(error));
	}
	
	private void addXmlError(ECPResponse response, String error, String errorDetail) {
		if (response.getErrorList() == null)
			response.setErrorList(new ArrayList<RestError>());
		RestError restError = new RestError();
		restError.setErrorShort(error);
		if (errorDetail != null)
			restError.setErrorDetail(errorDetail);
		response.getErrorList().add(restError);
	}
	
	private String resolveString(String key) {
		String enString = resolveString(key, Locale.ENGLISH);
		String deString = resolveString(key, Locale.GERMAN);
		
		StringBuffer sb = new StringBuffer();
		
		if (deString != null)
			sb.append(deString);
		
		if (deString != null && enString != null)
			sb.append(" / ");
		
		if (enString != null)
			sb.append(enString);
		
		if (sb.length() == 0)
			return null;
		else
			return sb.toString();
	}
	
	private String resolveString(String key, Locale locale) {
		try {
			TextPropertyEntity tpe = textPropertyDao.findByKeyAndLang(key, locale.getLanguage());

			if (tpe != null)
				return tpe.getValue();
			else {
				ResourceBundle bundle = ResourceBundle.getBundle("msg.messages", locale);		
				return bundle.getString(key);
			}
		}
		catch (Exception e) {
			return null;
		}
	}
}
