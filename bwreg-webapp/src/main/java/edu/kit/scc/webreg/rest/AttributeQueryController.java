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

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.TextPropertyDao;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.drools.exc.UnauthorizedException;
import edu.kit.scc.webreg.entity.TextPropertyEntity;
import edu.kit.scc.webreg.exc.GenericRestInterfaceException;
import edu.kit.scc.webreg.exc.LoginFailedException;
import edu.kit.scc.webreg.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.exc.NoServiceFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UserUpdateFailedException;
import edu.kit.scc.webreg.rest.dto.AttributeQueryResponse;
import edu.kit.scc.webreg.rest.dto.RestError;
import edu.kit.scc.webreg.service.UserUpdateService;

@Path("/attrq")
public class AttributeQueryController {

	@Inject
	private Logger logger;

	@Inject
	private UserUpdateService userUpdateService;

	@Inject
	private TextPropertyDao textPropertyDao;

	@GET
	@Path("/eppn/{service}/{eppn}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> attributeQuery(@PathParam("eppn") String eppn,
			@PathParam("service") String serviceShortName, @Context HttpServletRequest request)
			throws RestInterfaceException {
		return userUpdateService.updateUser(eppn, serviceShortName, request.getServerName(),
				"rest-/attrq/eppn/" + serviceShortName + "/" + eppn);
	}

	@GET
	@Path("/uid-number/{service}/{uidNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> attributeQueryUidNumber(@PathParam("uidNumber") Integer uidNumber,
			@PathParam("service") String serviceShortName, @Context HttpServletRequest request)
			throws RestInterfaceException {
		return userUpdateService.updateUser(uidNumber, serviceShortName, request.getServerName(),
				"rest-/attrq/uid-number/" + serviceShortName + "/" + uidNumber);
	}

	@GET
	@Path("/regid/{regid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> attributeQuery(@PathParam("regid") Long regId, @Context HttpServletRequest request)
			throws RestInterfaceException {
		return userUpdateService.updateUser(regId, request.getServerName(), "rest-/attrq/regid/" + regId);
	}

	@GET
	@Path("/generic/{service}/{key}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> attributeQueryByGeneric(@PathParam("key") String key, @PathParam("value") String value,
			@PathParam("service") String serviceShortName, @Context HttpServletRequest request)
			throws RestInterfaceException {
		return userUpdateService.updateUserByGenericStore(key, value, serviceShortName, request.getServerName(),
				"rest-/attrq/generic/" + serviceShortName + "/" + key + "/" + value);
	}

	@POST
	@Path("/generic-post/{service}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> attributeQueryByGenericPost(@FormParam("key") String key,
			@FormParam("value") String value, @PathParam("service") String serviceShortName,
			@Context HttpServletRequest request) throws RestInterfaceException {
		return userUpdateService.updateUserByGenericStore(key, value, serviceShortName, request.getServerName(),
				"rest-/attrq/generic/" + serviceShortName + "/" + key + "/" + value);
	}

	@GET
	@Path("/attribute/{service}/{key}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> attributeQueryByAttribute(@PathParam("key") String key, @PathParam("value") String value,
			@PathParam("service") String serviceShortName, @Context HttpServletRequest request)
			throws RestInterfaceException {
		return userUpdateService.updateUserByAttributeStore(key, value, serviceShortName, request.getServerName(),
				"rest-/attrq/generic/" + serviceShortName + "/" + key + "/" + value);
	}

	@POST
	@Path("/attribute-post/{service}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> attributeQueryByAttributePost(@FormParam("key") String key,
			@FormParam("value") String value, @PathParam("service") String serviceShortName,
			@Context HttpServletRequest request) throws RestInterfaceException {
		return userUpdateService.updateUserByAttributeStore(key, value, serviceShortName, request.getServerName(),
				"rest-/attrq/generic/" + serviceShortName + "/" + key + "/" + value);
	}

	@GET
	@Path("/eppn-xml/{service}/{eppn}")
	@Produces(MediaType.APPLICATION_XML)
	public AttributeQueryResponse attributeQueryXML(@PathParam("eppn") String eppn,
			@PathParam("service") String serviceShortName, @Context HttpServletRequest request)
			throws RestInterfaceException {
		AttributeQueryResponse response = new AttributeQueryResponse();

		try {
			userUpdateService.updateUser(eppn, serviceShortName, request.getServerName(),
					"rest-/attrq/eppn-xml/" + serviceShortName + "/" + eppn);
		} catch (NoUserFoundException e) {
			generateFailXml(response, 400, "ecp login failed", "user-not-found");
			return response;
		} catch (NoServiceFoundException e) {
			generateFailXml(response, 400, "ecp login failed", "no-such-service");
			return response;
		} catch (NoRegistryFoundException e) {
			generateFailXml(response, 400, "ecp login failed", "user-not-registered");
			return response;
		} catch (UserUpdateFailedException e) {
			generateFailXml(response, 400, "ecp login failed", "user-update-failed");
			return response;
		} catch (UnauthorizedException e) {
			response.setCode(405);
			response.setMessage("rules failed");

			for (UnauthorizedUser uu : e.getUnauthList()) {
				addXmlError(response, uu.getMessage(), resolveString(uu.getMessage()));
			}
			return response;
		} catch (LoginFailedException e) {
			generateFailXml(response, 405, "rules failed", e.getMessage());
			return response;
		} catch (GenericRestInterfaceException e) {
			generateFailXml(response, 500, "ecp login failed", "generic-error");
			return response;
		} catch (RestInterfaceException e) {
			logger.warn("Unmapped RestInterfaceException!", e);
			generateFailXml(response, 400, "ecp login failed", "unknown-error");
			return response;
		}

		response.setCode(200);
		response.setMessage("success");

		return response;
	}

	@GET
	@Path("/regid-xml/{regid}")
	@Produces(MediaType.APPLICATION_XML)
	public AttributeQueryResponse attributeQueryXML(@PathParam("regid") Long regId, @Context HttpServletRequest request)
			throws RestInterfaceException {
		AttributeQueryResponse response = new AttributeQueryResponse();

		try {
			userUpdateService.updateUser(regId, request.getServerName(), "rest-/attrq/regid-xml/" + regId);
		} catch (NoUserFoundException e) {
			generateFailXml(response, 400, "ecp login failed", "user-not-found");
			return response;
		} catch (NoServiceFoundException e) {
			generateFailXml(response, 400, "ecp login failed", "no-such-service");
			return response;
		} catch (NoRegistryFoundException e) {
			generateFailXml(response, 400, "ecp login failed", "user-not-registered");
			return response;
		} catch (UserUpdateFailedException e) {
			generateFailXml(response, 400, "ecp login failed", "user-update-failed");
			return response;
		} catch (UnauthorizedException e) {
			response.setCode(405);
			response.setMessage("rules failed");

			for (UnauthorizedUser uu : e.getUnauthList()) {
				addXmlError(response, uu.getMessage(), resolveString(uu.getMessage()));
			}
		} catch (GenericRestInterfaceException e) {
			generateFailXml(response, 500, "ecp login failed", "generic-error");
			return response;
		} catch (RestInterfaceException e) {
			logger.warn("Unmapped RestInterfaceException!", e);
			generateFailXml(response, 400, "ecp login failed", "unknown-error");
			return response;
		}

		response.setCode(200);
		response.setMessage("success");

		return response;
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
		} catch (Exception e) {
			return null;
		}
	}

	private void generateFailXml(AttributeQueryResponse response, int code, String message, String error) {
		response.setCode(code);
		response.setMessage(message);
		addXmlError(response, error, resolveString(error));
	}

	private void addXmlError(AttributeQueryResponse response, String error, String errorDetail) {
		if (response.getErrorList() == null)
			response.setErrorList(new ArrayList<RestError>());
		RestError restError = new RestError();
		restError.setErrorShort(error);
		if (errorDetail != null)
			restError.setErrorDetail(errorDetail);
		response.getErrorList().add(restError);
	}
}
