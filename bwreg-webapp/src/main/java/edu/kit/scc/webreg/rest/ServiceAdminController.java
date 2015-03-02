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

import edu.kit.scc.webreg.dto.entity.RegistryEntityDto;
import edu.kit.scc.webreg.dto.service.RegistryDtoService;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.rest.exc.NoItemFoundException;
import edu.kit.scc.webreg.rest.exc.UnauthorizedException;
import edu.kit.scc.webreg.sec.SecurityFilter;
import edu.kit.scc.webreg.service.AdminUserService;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;

@Path("/service-admin")
public class ServiceAdminController {

	@Inject
	private RegistryService registryService;

	@Inject
	private RegistryDtoService registryDtoService;
	
	@Inject
	private UserService userService;

	@Inject
	private AdminUserService adminUserService;

	@Inject
	private RoleService roleService;

	@Inject
	private ServiceService serviceService;
	
	@Path(value = "/depro/list/{ssn}")
	@Produces({"application/json"})
	@GET
	public List<RegistryEntityDto> list(@PathParam("ssn") String ssn, @Context HttpServletRequest request)
					throws IOException, UnauthorizedException {
		
		ServiceEntity serviceEntity = serviceService.findByShortName(ssn);

		if (request.getAttribute(SecurityFilter.USER_ID) != null &&
				request.getAttribute(SecurityFilter.USER_ID) instanceof Long) {
			Long userId = (Long) request.getAttribute(SecurityFilter.USER_ID);
			Boolean check = roleService.checkUserInRole(userId, serviceEntity.getAdminRole().getName());
			if (! check)
				throw new UnauthorizedException("No access");
		}
		else if (request.getAttribute(SecurityFilter.ADMIN_USER_ID) != null &&
				request.getAttribute(SecurityFilter.ADMIN_USER_ID) instanceof Long) {
			Long adminUserId = (Long) request.getAttribute(SecurityFilter.ADMIN_USER_ID);
			Boolean check = roleService.checkAdminUserInRole(adminUserId, serviceEntity.getAdminRole().getName());
			if (! check)
				throw new UnauthorizedException("No access");
		}
		
		List<RegistryEntityDto> deproList = registryDtoService.findRegistriesForDepro(serviceEntity.getShortName());

		return deproList;
	}
}
