package edu.kit.scc.webreg.rest;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import edu.kit.scc.webreg.drools.exc.UnauthorizedException;
import edu.kit.scc.webreg.dto.entity.GroupEntityDto;
import edu.kit.scc.webreg.dto.service.GroupDtoService;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.sec.SecurityFilter;

@Path("/group-admin")
public class GroupController {

	@Inject
	private GroupDtoService groupDtoService;

	@Path(value = "/find/id/{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	public GroupEntityDto findById(@PathParam("id") Long id, @Context HttpServletRequest request)
			throws RestInterfaceException {
		return groupDtoService.findById(id, resolveUserId(request), false);
	}

	@Path(value = "/find/name/{name}")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	public GroupEntityDto findByName(@PathParam("name") String name, @Context HttpServletRequest request)
			throws RestInterfaceException {
		return groupDtoService.findByName(name, resolveUserId(request), false);
	}

	@Path(value = "/find-detail/id/{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	public GroupEntityDto findDetailById(@PathParam("id") Long id, @Context HttpServletRequest request)
			throws RestInterfaceException {
		return groupDtoService.findById(id, resolveUserId(request), true);
	}

	@Path(value = "/find-detail/name/{name}")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	public GroupEntityDto findDetailByName(@PathParam("name") String name, @Context HttpServletRequest request)
			throws RestInterfaceException {
		return groupDtoService.findByName(name, resolveUserId(request), true);
	}

	@Path(value = "/add/groupId/{groupId}/userId/{userId}")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	public GroupEntityDto addUserToGroup(@PathParam("groupId") Long groupId, @PathParam("userId") Long userId,
			@Context HttpServletRequest request) throws RestInterfaceException {
		return groupDtoService.addUserToGroup(groupId, userId, resolveUserId(request));
	}

	@Path(value = "/remove/groupId/{groupId}/userId/{userId}")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	public GroupEntityDto removeUserFromGroup(@PathParam("groupId") Long groupId, @PathParam("userId") Long userId,
			@Context HttpServletRequest request) throws RestInterfaceException {
		return groupDtoService.removeUserToGroup(groupId, userId, resolveUserId(request));
	}

	@Path(value = "/create/{ssn}/{name}")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	public GroupEntityDto create(@PathParam("ssn") String ssn, @PathParam("name") String name,
			@Context HttpServletRequest request) throws RestInterfaceException {
		return groupDtoService.create(ssn, name, resolveUserId(request));
	}

	protected Long resolveUserId(HttpServletRequest request) throws UnauthorizedException {
		if (request.getAttribute(SecurityFilter.IDENTITY_ID) != null
				&& request.getAttribute(SecurityFilter.IDENTITY_ID) instanceof Long) {
			return (Long) request.getAttribute(SecurityFilter.IDENTITY_ID);
		} else if (request.getAttribute(SecurityFilter.ADMIN_USER_ID) != null
				&& request.getAttribute(SecurityFilter.ADMIN_USER_ID) instanceof Long) {
			return (Long) request.getAttribute(SecurityFilter.ADMIN_USER_ID);
		} else {
			throw new UnauthorizedException("No user is set");
		}
	}
}
