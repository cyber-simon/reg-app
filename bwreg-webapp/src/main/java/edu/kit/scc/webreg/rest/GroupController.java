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
import edu.kit.scc.webreg.exc.UnauthorizedException;
import edu.kit.scc.webreg.sec.SecurityFilter;

@Path("/group-admin")
public class GroupController {

	@Inject
	private GroupDtoService groupDtoService;
	
	@Path(value = "/find/id/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public GroupEntityDto findById(@PathParam("id") Long id, @Context HttpServletRequest request)
				throws IOException, RestInterfaceException, ServletException {
		return groupDtoService.findById(id, resolveUserId(request), false);
	}

	@Path(value = "/find/name/{name}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public GroupEntityDto findByName(@PathParam("name") String name, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		return groupDtoService.findByName(name, resolveUserId(request), false);
	}

	@Path(value = "/find-detail/id/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public GroupEntityDto findDetailById(@PathParam("id") Long id, @Context HttpServletRequest request)
				throws IOException, RestInterfaceException, ServletException {
		return groupDtoService.findById(id, resolveUserId(request), true);
	}

	@Path(value = "/find-detail/name/{name}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public GroupEntityDto findDetailByName(@PathParam("name") String name, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		return groupDtoService.findByName(name, resolveUserId(request), true);
	}

	@Path(value = "/create/{ssn}/{name}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public GroupEntityDto create(@PathParam("ssn") String ssn, @PathParam("name") String name, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		return groupDtoService.create(ssn, name, resolveUserId(request));
	}

	protected Long resolveUserId(HttpServletRequest request)
			throws UnauthorizedException {
		if (request.getAttribute(SecurityFilter.USER_ID) != null &&
				request.getAttribute(SecurityFilter.USER_ID) instanceof Long) {
			return (Long) request.getAttribute(SecurityFilter.USER_ID);
		}
		else if (request.getAttribute(SecurityFilter.ADMIN_USER_ID) != null &&
				request.getAttribute(SecurityFilter.ADMIN_USER_ID) instanceof Long) {
			return (Long) request.getAttribute(SecurityFilter.ADMIN_USER_ID);
		}
		else {
			throw new UnauthorizedException("No user is set");
		}
	}
}
