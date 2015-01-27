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
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.rest.exc.LoginFailedException;
import edu.kit.scc.webreg.rest.exc.NoItemFoundException;
import edu.kit.scc.webreg.rest.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.rest.exc.NoServiceFoundException;
import edu.kit.scc.webreg.rest.exc.NoUserFoundException;
import edu.kit.scc.webreg.rest.exc.RestInterfaceException;
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
		return attributeQueryIntern(eppn, serviceShortName);
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
		
		return attributeQueryIntern(registry.getUser().getEppn(), registry.getService().getShortName());
	}
	
	private Map<String, String> attributeQueryIntern(String eppn, String serviceShortName)
			throws IOException, ServletException, RestInterfaceException {

		ServiceEntity service = serviceService.findByShortName(serviceShortName);
		service = serviceService.findByIdWithServiceProps(service.getId());
		
		if (service == null)
			throw new NoServiceFoundException("No such service");
		
		UserEntity user = userService.findByEppn(eppn);
		
		if (user == null)
			throw new NoUserFoundException("No such user");
		
		user = userService.findByIdWithStore(user.getId());
		
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
	
				user = userUpdateService.updateUserFromIdp(user);
			}
		} catch (RegisterException e) {
			logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
			throw new NoItemFoundException("user update failed: " + e.getMessage());
		}
		
		RegistryEntity registry = registryService.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
		
		if (registry == null) {
			/*
			 * Also check for Lost_access registries. They should also be allowed to be rechecked.
			 */
			registry = registryService.findByServiceAndUserAndStatus(service, user, RegistryStatus.LOST_ACCESS);
			
			if (registry == null) {
				throw new NoRegistryFoundException("No such registry");
			}
		}
		
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

}
