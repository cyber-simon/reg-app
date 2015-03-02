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
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.rest.exc.NoItemFoundException;
import edu.kit.scc.webreg.sec.SecurityFilter;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.util.SessionManager;

@Path("/service-admin")
public class ServiceAdminController {

	@Inject
	private RegistryService registryService;

	@Inject
	private RegistryDtoService registryDtoService;
	
	@Inject
	private UserService userService;
	
	@Inject
	private ServiceService serviceService;
	
	@Inject
	private SessionManager sessionManager;
	
	@Path(value = "/depro/list/{ssn}")
	@Produces({"application/json"})
	@GET
	public List<RegistryEntityDto> list(@PathParam("ssn") String ssn, @Context HttpServletRequest request)
					throws IOException, NoItemFoundException {
		
		ServiceEntity serviceEntity = serviceService.findByShortName(ssn);
		System.out.println("" + sessionManager.getUserId());
		System.out.println("" + request.getAttribute(SecurityFilter.ADMIN_USER));
		List<RegistryEntityDto> deproList = registryDtoService.findRegistriesForDepro(serviceEntity.getShortName());

		return deproList;
	}


}
