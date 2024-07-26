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

import java.util.List;

import org.slf4j.Logger;

import edu.kit.scc.webreg.drools.exc.UnauthorizedException;
import edu.kit.scc.webreg.dto.entity.ProjectEntityDto;
import edu.kit.scc.webreg.dto.service.ProjectDtoService;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.NoItemFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.sec.SecurityFilter;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.ServiceService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;

@Path("/project-service-admin")
public class ProjectServiceAdminController {

	@Inject
	private Logger logger;

	@Inject
	private RoleService roleService;

	@Inject
	private ServiceService serviceService;

	@Inject
	private ProjectDtoService projectDtoService;

	@Path(value = "/find-all/{ssn}")
	@Produces({ "application/json" })
	@GET
	public List<ProjectEntityDto> findAll(@PathParam("ssn") String ssn,
			@Context HttpServletRequest request) throws RestInterfaceException {

		ServiceEntity serviceEntity = serviceService.findByShortName(ssn);
		if (serviceEntity == null)
			throw new NoItemFoundException("No such service");

		if (!checkAccess(request, serviceEntity.getProjectAdminRole().getName()))
			throw new UnauthorizedException("No access");

		List<ProjectEntityDto> projectList = projectDtoService.findByService(serviceEntity);

		return projectList;
	}

	protected Boolean checkAccess(HttpServletRequest request, String roleName) {
		Boolean check;

		if (request.getAttribute(SecurityFilter.IDENTITY_ID) != null
				&& request.getAttribute(SecurityFilter.IDENTITY_ID) instanceof Long) {
			Long identityId = (Long) request.getAttribute(SecurityFilter.IDENTITY_ID);
			check = roleService.checkIdentityInRole(identityId, roleName);
		} else if (request.getAttribute(SecurityFilter.ADMIN_USER_ID) != null
				&& request.getAttribute(SecurityFilter.ADMIN_USER_ID) instanceof Long) {
			Long adminUserId = (Long) request.getAttribute(SecurityFilter.ADMIN_USER_ID);
			check = roleService.checkAdminUserInRole(adminUserId, roleName);
		} else {
			check = Boolean.FALSE;
		}

		return check;
	}

	protected String resolveUsername(HttpServletRequest request) {
		if (request.getAttribute(SecurityFilter.IDENTITY_ID) != null
				&& request.getAttribute(SecurityFilter.IDENTITY_ID) instanceof Long) {
			Long identityId = (Long) request.getAttribute(SecurityFilter.IDENTITY_ID);
			return "identity-" + identityId;
		} else if (request.getAttribute(SecurityFilter.ADMIN_USER_ID) != null
				&& request.getAttribute(SecurityFilter.ADMIN_USER_ID) instanceof Long) {
			Long adminUserId = (Long) request.getAttribute(SecurityFilter.ADMIN_USER_ID);
			return "adminuser-" + adminUserId;
		} else
			return null;
	}
}
