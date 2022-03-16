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
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dto.entity.RegistryEntityDto;
import edu.kit.scc.webreg.dto.service.RegistryDtoService;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.NoItemFoundException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UnauthorizedException;
import edu.kit.scc.webreg.sec.SecurityFilter;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;

@Path("/service-admin")
public class ServiceAdminController {

	@Inject
	private Logger logger;
	
	@Inject
	private RegistryService registryService;

	@Inject
	private RegistryDtoService registryDtoService;
	
    @Inject
    private RegisterUserService registerUserService;

	@Inject
	private RoleService roleService;

	@Inject
	private ServiceService serviceService;

	@Path(value = "/bystatus/{ssn}/{status}")
	@Produces({"application/json"})
	@GET
	public List<RegistryEntityDto> findByStatus(@PathParam("ssn") String ssn, @PathParam("status") String status, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException {
		
		ServiceEntity serviceEntity = serviceService.findByShortName(ssn);
		if (serviceEntity == null)
			throw new NoItemFoundException("No such service");

		if (! checkAccess(request, serviceEntity.getAdminRole().getName()))
			throw new UnauthorizedException("No access");
		
		if (status == null)
			throw new NoItemFoundException("status must not be null");
		
		RegistryStatus registryStatus;
		try {
			registryStatus = RegistryStatus.valueOf(status);
		} catch (IllegalArgumentException e) {
			throw new NoItemFoundException("No such RegistryStatus. Possible values are: ACTIVE, PENDING, LOST_ACCESS, ON_HOLD, DELETED, DEPROVISIONED");
		}
		
		List<RegistryEntityDto> deproList = registryDtoService.findRegistriesByStatus(serviceEntity, registryStatus);

		return deproList;
	}	
	
	@Path(value = "/byattribute/{ssn}/{key}/{value}")
	@Produces({"application/json"})
	@GET
	public List<RegistryEntityDto> findByAttribute(@PathParam("ssn") String ssn, @PathParam("key") String key, @PathParam("value") String value, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException {
		
		ServiceEntity serviceEntity = serviceService.findByShortName(ssn);
		if (serviceEntity == null)
			throw new NoItemFoundException("No such service");

		if (! checkAccess(request, serviceEntity.getAdminRole().getName()))
			throw new UnauthorizedException("No access");
		
		if (key == null)
			throw new NoItemFoundException("key must not be null");
		if (value == null)
			throw new NoItemFoundException("value must not be null");

		List<RegistryEntityDto> deproList = registryDtoService.findRegistriesByAttribute(key, value, serviceEntity);

		return deproList;
	}	
	
	@Path(value = "/depro/{ssn}/list")
	@Produces({"application/json"})
	@GET
	public List<RegistryEntityDto> list(@PathParam("ssn") String ssn, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException {
		
		ServiceEntity serviceEntity = serviceService.findByShortName(ssn);
		if (serviceEntity == null)
			throw new NoItemFoundException("No such service");

		if (! checkAccess(request, serviceEntity.getAdminRole().getName()))
			throw new UnauthorizedException("No access");
		
		List<RegistryEntityDto> deproList = registryDtoService.findRegistriesForDepro(serviceEntity.getShortName());

		return deproList;
	}

	@Path(value = "/depro/{ssn}/cleanup/{regId}")
	@Produces({"application/json"})
	@GET
	public Response depro(@PathParam("ssn") String ssn, @PathParam("regId") Long regId, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, RegisterException {
		
		ServiceEntity serviceEntity = serviceService.findByShortName(ssn);
		if (serviceEntity == null)
			throw new NoItemFoundException("No such service");

		if (! checkAccess(request, serviceEntity.getAdminRole().getName()))
			throw new UnauthorizedException("No access");

		RegistryEntity registryEntity = registryService.findById(regId);
		if (registryEntity == null)
			throw new NoItemFoundException("No such registry");
		
		try {
			registerUserService.deprovision(registryEntity, resolveUsername(request));
			return Response.ok("registry deprovisioned", MediaType.TEXT_PLAIN_TYPE).build();
		} catch (RegisterException e) {
			logger.warn("Deprovision failed!", e);
			throw e;
		}
	}
	
	protected Boolean checkAccess(HttpServletRequest request, String roleName) {
		Boolean check;
		
		if (request.getAttribute(SecurityFilter.IDENTITY_ID) != null &&
				request.getAttribute(SecurityFilter.IDENTITY_ID) instanceof Long) {
			Long identityId = (Long) request.getAttribute(SecurityFilter.IDENTITY_ID);
			check = roleService.checkIdentityInRole(identityId, roleName);
		}
		else if (request.getAttribute(SecurityFilter.ADMIN_USER_ID) != null &&
				request.getAttribute(SecurityFilter.ADMIN_USER_ID) instanceof Long) {
			Long adminUserId = (Long) request.getAttribute(SecurityFilter.ADMIN_USER_ID);
			check = roleService.checkAdminUserInRole(adminUserId, roleName);
		}
		else {
			check = Boolean.FALSE;
		}
		
		return check;
	}
	
	protected String resolveUsername(HttpServletRequest request) {
		if (request.getAttribute(SecurityFilter.IDENTITY_ID) != null &&
				request.getAttribute(SecurityFilter.IDENTITY_ID) instanceof Long) {
			Long identityId = (Long) request.getAttribute(SecurityFilter.IDENTITY_ID);
			return "identity-" + identityId;
		}
		else if (request.getAttribute(SecurityFilter.ADMIN_USER_ID) != null &&
				request.getAttribute(SecurityFilter.ADMIN_USER_ID) instanceof Long) {
			Long adminUserId = (Long) request.getAttribute(SecurityFilter.ADMIN_USER_ID);
			return "adminuser-" + adminUserId;
		}
		else
			return null;
	}
}
