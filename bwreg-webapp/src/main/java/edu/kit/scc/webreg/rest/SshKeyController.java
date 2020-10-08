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

import edu.kit.scc.webreg.dto.entity.SshPubKeyEntityDto;
import edu.kit.scc.webreg.dto.service.SshPubKeyDtoService;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.exc.NoItemFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UnauthorizedException;
import edu.kit.scc.webreg.sec.SecurityFilter;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.ssh.SshLoginService;

@Path("/ssh-key")
public class SshKeyController {

	@Inject
	private RoleService roleService;
	
	@Inject
	private ServiceService serviceService;
	
	@Inject
	private SshLoginService sshLoginService;
	
	@Inject
	private SshPubKeyDtoService dtoService;
	
	@Path(value = "/list/uidnumber/{uidNumber}/all")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public List<SshPubKeyEntityDto> listKeysForUser(@PathParam("uidNumber") Long uidNumber, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException {
		return dtoService.findByUidNumber(uidNumber);
	}
	
	@Path(value = "/list/uidnumber/{uidNumber}/key-status/{status}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public List<SshPubKeyEntityDto> listKeysForUserAndStatus(@PathParam("uidNumber") Long uidNumber,
			@PathParam("status") SshPubKeyStatus keyStatus, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException {
		return dtoService.findByUidNumberAndStatus(uidNumber, keyStatus);
	}
	
	@Path(value = "/auth/all/{ssn}/uidnumber/{uidNumber}")
	@Produces({MediaType.TEXT_PLAIN})
	@GET
	public String authByUidNumber(@PathParam("ssn") String ssn, 
			@PathParam("uidNumber") Long uidNumber, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException {
		
		ServiceEntity service = serviceService.findByShortName(ssn);
		if (service == null)
			throw new NoItemFoundException("No such service");

		if (! checkAccess(request, service.getAdminRole().getName()))
			throw new UnauthorizedException("No access");

		return sshLoginService.authByUidNumber(service, uidNumber, request);
	}

	@Path(value = "/auth/interactive/{ssn}/uidnumber/{uidNumber}")
	@Produces({MediaType.TEXT_PLAIN})
	@GET
	public String authByUidNumberInteractive(@PathParam("ssn") String ssn, 
			@PathParam("uidNumber") Long uidNumber, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException {
		
		ServiceEntity service = serviceService.findByShortName(ssn);
		if (service == null)
			throw new NoItemFoundException("No such service");

		if (! checkAccess(request, service.getAdminRole().getName()))
			throw new UnauthorizedException("No access");

		return sshLoginService.authByUidNumberInteractive(service, uidNumber, request);
	}

	@Path(value = "/auth/command/{ssn}/uidnumber/{uidNumber}")
	@Produces({MediaType.TEXT_PLAIN})
	@GET
	public String authByUidNumberCommand(@PathParam("ssn") String ssn, 
			@PathParam("uidNumber") Long uidNumber, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException {
		
		
		ServiceEntity service = serviceService.findByShortName(ssn);
		if (service == null)
			throw new NoItemFoundException("No such service");

		if (! checkAccess(request, service.getAdminRole().getName()))
			throw new UnauthorizedException("No access");

		return sshLoginService.authByUidNumberCommand(service, uidNumber, request);
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
