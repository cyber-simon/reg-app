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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.rest.dto.AttributeQueryResponse;
import edu.kit.scc.webreg.rest.dto.RestError;
import edu.kit.scc.webreg.rest.exc.LoginFailedException;
import edu.kit.scc.webreg.rest.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.rest.exc.NoServiceFoundException;
import edu.kit.scc.webreg.rest.exc.NoUserFoundException;
import edu.kit.scc.webreg.rest.exc.RestInterfaceException;
import edu.kit.scc.webreg.rest.exc.UserUpdateFailedException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.UserUpdateService;

@Path("/attrq")
public class AttributeQueryController {

	@Inject
	private Logger logger;
	
	@Inject
	private UserService userService;

	@Inject
	private ServiceService serviceService;
	
	@Inject
	private RegistryService registryService;
	
	@Inject
	private UserUpdateService userUpdateService;
	
	@Inject
	private KnowledgeSessionService knowledgeSessionService;
	
	@GET
	@Path("/eppn/{service}/{eppn}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> attributeQuery(@PathParam("eppn") String eppn,
			@PathParam("service") String serviceShortName)
			throws IOException, ServletException, RestInterfaceException {
		return attributeQueryInternJSON(eppn, serviceShortName);
	}
	
	@GET
	@Path("/regid/{regid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> attributeQuery(@PathParam("regid") Long regId)
			throws IOException, ServletException, RestInterfaceException {
		RegistryEntity registry = registryService.findById(regId);

		if (registry == null) {
			logger.info("No registry found for id {}", regId);
			throw new NoRegistryFoundException("No such registry");
		}
		
		return attributeQueryInternJSON(registry.getUser().getEppn(), registry.getService().getShortName());
	}

	@GET
	@Path("/eppn-xml/{service}/{eppn}")
	@Produces(MediaType.APPLICATION_XML)
	public AttributeQueryResponse attributeQueryXML(@PathParam("eppn") String eppn,
			@PathParam("service") String serviceShortName)
			throws IOException, ServletException, RestInterfaceException {
		return attributeQueryInternXML(eppn, serviceShortName);
	}
	
	@GET
	@Path("/regid-xml/{regid}")
	@Produces(MediaType.APPLICATION_XML)
	public AttributeQueryResponse attributeQueryXML(@PathParam("regid") Long regId)
			throws IOException, ServletException, RestInterfaceException {
		RegistryEntity registry = registryService.findById(regId);

		if (registry == null) {
			logger.info("No registry found for id {}", regId);
			throw new NoRegistryFoundException("No such registry");
		}
		
		return attributeQueryInternXML(registry.getUser().getEppn(), registry.getService().getShortName());
	}

	private AttributeQueryResponse attributeQueryInternXML(String eppn, String serviceShortName) {
		AttributeQueryResponse response = new AttributeQueryResponse();

		ServiceEntity service = findService(eppn, serviceShortName);
		
		if (service == null) {
			generateFailXml(response, 400, "attribute query failed", "no-such-service", "Service does not exist");
			return response;
		}

		UserEntity user = findUser(eppn);
		
		if (user == null) {
			generateFailXml(response, 400, "attribute query failed", "no-such-user", "User does not exist");
			return response;
		}
	
		try {
			updateUser(user, service);
		} catch (UserUpdateFailedException e) {
			generateFailXml(response, 400, "attribute query failed", "user-update-failed", "User update failed: " + e.getMessage());
			return response;
		}

		RegistryEntity registry = findRegistry(user, service);

		if (registry == null) {
			generateFailXml(response, 400, "attribute query failed", "no-registry-found", "User is not registered for service");
			return response;
		}

		List<Object> objectList = checkRules(user, service, registry);
		List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
		List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);
		
		if (unauthorizedUserList.size() == 0 || overrideAccessList.size() > 0) {
			response.setCode(200);
			response.setMessage("success");
		}
		else {
			response.setCode(405);
			response.setMessage("rules failed");
			
			for (UnauthorizedUser uu : unauthorizedUserList) {
				addXmlError(response, uu.getMessage(), "");
			}
		}
		
		return response;
	}
	
	private Map<String, String> attributeQueryInternJSON(String eppn, String serviceShortName)
			throws RestInterfaceException {

		ServiceEntity service = findService(eppn, serviceShortName);
		
		if (service == null)
			throw new NoServiceFoundException("No such service");
		
		UserEntity user = findUser(eppn);
		
		if (user == null)
			throw new NoUserFoundException("No such user");

		updateUser(user, service);
		
		RegistryEntity registry = findRegistry(user, service);

		if (registry == null)
			throw new NoRegistryFoundException("No such registry");
		
		List<Object> objectList = checkRules(user, service, registry);
		
		StringBuilder sb = new StringBuilder();
		for (Object o : objectList) {
			if (o instanceof OverrideAccess) {
				objectList.clear();
				sb.setLength(0);
				logger.debug("Removing requirements due to OverrideAccess");
				break;
			}
			else if (o instanceof UnauthorizedUser) {
				String s = ((UnauthorizedUser) o).getMessage();
				sb.append(s);
				sb.append("\n");
			}
		}

		if (sb.length() > 0) {
			throw new LoginFailedException("user not allowd for service\n" + sb.toString());
		}

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Map<String, String> map = new HashMap<String, String>();
		map.put("eppn", user.getEppn());
		map.put("email", user.getEmail());
		map.put("last_update",  df.format(user.getLastUpdate()));
		
		return map;
	}
	
	private ServiceEntity findService(String eppn, String serviceShortName) {
		ServiceEntity service = serviceService.findByShortName(serviceShortName);
		
		if (service != null) {
			service = serviceService.findByIdWithServiceProps(service.getId());
		}
		
		return service;
	}

	private UserEntity findUser(String eppn) {
		UserEntity user = userService.findByEppn(eppn);

		if (user != null) {
			user = userService.findByIdWithStore(user.getId());
		}

		return user;
	}
	
	private void updateUser(UserEntity user, ServiceEntity service) throws UserUpdateFailedException {
		// Default expiry Time after which an attrq is issued to IDP in millis
		Long expireTime = 10000L;
		
		if (service.getServiceProps() != null && service.getServiceProps().containsKey("attrq_expire_time")) {
			expireTime = Long.parseLong(service.getServiceProps().get("attrq_expire_time"));
		}
		
		try {
			if ((System.currentTimeMillis() - user.getLastUpdate().getTime()) < expireTime) {
				logger.info("Skipping attributequery for {} with {}@{}", new Object[] {user.getEppn(), 
						user.getPersistentId(), user.getIdp().getEntityId()});
			}
			else {
				logger.info("Performing attributequery for {} with {}@{}", new Object[] {user.getEppn(), 
						user.getPersistentId(), user.getIdp().getEntityId()});
	
				user = userUpdateService.updateUserFromIdp(user, service);
			}
		} catch (UserUpdateException e) {
			logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
			throw new UserUpdateFailedException("user update failed: " + e.getMessage());
		}		
	}
	
	private RegistryEntity findRegistry(UserEntity user, ServiceEntity service) {
		RegistryEntity registry = registryService.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
		
		if (registry == null) {
			/*
			 * Also check for Lost_access registries. They should also be allowed to be rechecked.
			 */
			registry = registryService.findByServiceAndUserAndStatus(service, user, RegistryStatus.LOST_ACCESS);
		}
		
		return registry;
	}
	
	private List<Object> checkRules(UserEntity user, ServiceEntity service, RegistryEntity registry) {
		List<Object> objectList;
		
		if (service.getAccessRule() == null) {
			objectList = knowledgeSessionService.checkRule("default", "permitAllRule", "1.0.0", user, service, registry, "user-self", false);
		}
		else {
			BusinessRulePackageEntity rulePackage = service.getAccessRule().getRulePackage();
			if (rulePackage != null) {
				objectList = knowledgeSessionService.checkRule(rulePackage.getPackageName(), rulePackage.getKnowledgeBaseName(), 
					rulePackage.getKnowledgeBaseVersion(), user, service, registry, "user-self", false);
			}
			else {
				throw new IllegalStateException("checkServiceAccess called with a rule (" +
							service.getAccessRule().getName() + ") that has no rulePackage");
			}
		}

		return objectList;
	}
	
	private void generateFailXml(AttributeQueryResponse response, int code, String message, String error, String errorDetail) {
		response.setCode(code);
		response.setMessage(message);
		addXmlError(response, error, errorDetail);
	}
	
	private void addXmlError(AttributeQueryResponse response, String error, String errorDetail) {
		if (response.getErrorList() == null)
			response.setErrorList(new ArrayList<RestError>());
		RestError restError = new RestError();
		restError.setErrorShort(error);
		restError.setErrorDetail(errorDetail);
		response.getErrorList().add(restError);
	}
	
	private List<OverrideAccess> extractOverideAccess(List<Object> objectList) {
		List<OverrideAccess> returnList = new ArrayList<OverrideAccess>();
		
		for (Object o : objectList) {
			if (o instanceof OverrideAccess) {
				returnList.add((OverrideAccess) o);
			}
		}
		
		return returnList;
	}

	private List<UnauthorizedUser> extractUnauthorizedUser(List<Object> objectList) {
		List<UnauthorizedUser> returnList = new ArrayList<UnauthorizedUser>();
		
		for (Object o : objectList) {
			if (o instanceof UnauthorizedUser) {
				returnList.add((UnauthorizedUser) o);
			}
		}

		return returnList;
	}
	
}
