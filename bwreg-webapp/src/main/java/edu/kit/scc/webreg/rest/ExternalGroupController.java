package edu.kit.scc.webreg.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import edu.kit.scc.webreg.dto.entity.GroupEntityDto;
import edu.kit.scc.webreg.dto.service.GroupDtoService;
import edu.kit.scc.webreg.exc.RestInterfaceException;

@Path("/external-group")
public class ExternalGroupController {

	@Inject
	GroupDtoService groupDtoService;
	
	@Path(value = "/find/id/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public GroupEntityDto updateUser(@PathParam("id") Long id, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		return groupDtoService.findById(id);
	}

	@Path(value = "/find/name/{name}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public GroupEntityDto updateUser(@PathParam("name") String name, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		return groupDtoService.findByName(name);
	}
}
