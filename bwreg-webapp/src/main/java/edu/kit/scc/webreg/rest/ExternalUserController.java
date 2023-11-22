package edu.kit.scc.webreg.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;

import edu.kit.scc.webreg.drools.exc.UnauthorizedException;
import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.dto.service.ExternalUserDtoService;
import edu.kit.scc.webreg.entity.ExternalUserAdminRoleEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.sec.SecurityFilter;
import edu.kit.scc.webreg.service.AdminUserService;
import edu.kit.scc.webreg.service.RoleService;

@Path("/external-user")
public class ExternalUserController {

	@Inject 
	private Logger logger;
	
	@Inject
	private ExternalUserDtoService externalUserDtoService;
	
	@Inject
	private RoleService roleService;
	
	@Inject
	private AdminUserService adminUserService;
	
	@Path(value = "/create")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@POST
	public Map<String, String> create(@Valid ExternalUserEntityDto externalUserDto, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {

		logger.info("Create external user called for {}", externalUserDto.getExternalId());
		ExternalUserAdminRoleEntity role = resolveAdminRole(request, null);
		externalUserDtoService.createExternalUser(externalUserDto, role);
		
		Map<String, String> map = new HashMap<>();
		map.put("result", "success");
		return map;
	}

	@Path(value = "/update")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@POST
	public Map<String, String> update(@Valid ExternalUserEntityDto externalUserDto, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {

		logger.info("Update external user called for {}", externalUserDto.getExternalId());
		
		ExternalUserAdminRoleEntity role = resolveAdminRole(request);
		externalUserDtoService.updateExternalUser(externalUserDto, role);
		
		Map<String, String> map = new HashMap<>();
		map.put("result", "success");
		return map;
	}

	@Path(value = "/find/externalId/{externalId}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public ExternalUserEntityDto find(@PathParam("externalId") String externalId, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		return externalUserDtoService.findByExternalId(externalId);
	}

	@Path(value = "/activate/externalId/{externalId}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public void activateUser(@PathParam("externalId") String externalId, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		ExternalUserAdminRoleEntity role = resolveAdminRole(request);
		externalUserDtoService.activateExternalUser(externalId, role);
	}

	@Path(value = "/deactivate/externalId/{externalId}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public void deactivateUser(@PathParam("externalId") String externalId, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		ExternalUserAdminRoleEntity role = resolveAdminRole(request);
		externalUserDtoService.deactivateExternalUser(externalId, role);
	}
	
	@Path(value = "/find/attribute/{key}/{value}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public List<ExternalUserEntityDto> findByAttribute(@PathParam("key") String key, @PathParam("value") String value, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		ExternalUserAdminRoleEntity role = resolveAdminRole(request);
		return externalUserDtoService.findByAttribute(key, value, role);
	}

	@Path(value = "/find/generic/{key}/{value}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public List<ExternalUserEntityDto> findByGeneric(@PathParam("key") String key, @PathParam("value") String value, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		ExternalUserAdminRoleEntity role = resolveAdminRole(request);
		return externalUserDtoService.findByGeneric(key, value, role);
	}

	@Path(value = "/find/all")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public List<ExternalUserEntityDto> findAll(@Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		ExternalUserAdminRoleEntity role = resolveAdminRole(request);
		return externalUserDtoService.findAll(role);
	}

	protected ExternalUserAdminRoleEntity resolveAdminRole(HttpServletRequest request)
			throws UnauthorizedException {
		return resolveAdminRole(request, null);
	}
	
	protected ExternalUserAdminRoleEntity resolveAdminRole(HttpServletRequest request, String preferredRoleName)
			throws UnauthorizedException {
		List<RoleEntity> roleList;
		if (request.getAttribute(SecurityFilter.IDENTITY_ID) != null &&
				request.getAttribute(SecurityFilter.IDENTITY_ID) instanceof Long) {
			Long identityId = (Long) request.getAttribute(SecurityFilter.IDENTITY_ID);
			roleList = roleService.findByIdentityId(identityId);
		}
		else if (request.getAttribute(SecurityFilter.ADMIN_USER_ID) != null &&
				request.getAttribute(SecurityFilter.ADMIN_USER_ID) instanceof Long) {
			Long adminUserId = (Long) request.getAttribute(SecurityFilter.ADMIN_USER_ID);
			roleList = adminUserService.findRolesForUserById(adminUserId);
		}
		else {
			throw new UnauthorizedException("No user is set");
		}

		if (preferredRoleName == null) {
			preferredRoleName = request.getParameter("role");
		}
		
		for (RoleEntity role : roleList) {
			if (role instanceof ExternalUserAdminRoleEntity) {
				if (preferredRoleName == null) {
					return (ExternalUserAdminRoleEntity) role;
				}
				else if (role.getName().equals(preferredRoleName)) {
					return (ExternalUserAdminRoleEntity) role;
				}
			}
		}
		
		throw new UnauthorizedException("No ExternalUserAdminRoleEntity found for user");
	}
}
