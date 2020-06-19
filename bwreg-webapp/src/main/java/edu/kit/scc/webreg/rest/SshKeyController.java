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

import org.slf4j.Logger;

import edu.kit.scc.webreg.dto.entity.SshPubKeyEntityDto;
import edu.kit.scc.webreg.dto.service.SshPubKeyDtoService;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryStatus;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.entity.SshPubKeyUsageType;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.NoItemFoundException;
import edu.kit.scc.webreg.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UnauthorizedException;
import edu.kit.scc.webreg.sec.SecurityFilter;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.ssh.SshPubKeyRegistryService;

@Path("/ssh-key")
public class SshKeyController {

	@Inject 
	private Logger logger;
		
	@Inject
	private RoleService roleService;
	
	@Inject
	private ServiceService serviceService;
	
	@Inject
	private RegistryService registryService;

	@Inject
	private UserService userService;
	
	@Inject
	private SshPubKeyDtoService dtoService;
	
	@Inject
	private SshPubKeyRegistryService sshPubKeyRegistryService;

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

		UserEntity user = userService.findByUidNumber(uidNumber);
		if (user == null)
			throw new NoUserFoundException("No such user");
		
		logger.debug("Searching for active registry for user {} and service {}", user.getId(), service.getShortName());
		RegistryEntity registry = registryService.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
		
		if (registry == null)
			throw new NoRegistryFoundException("No active registry for user");
		
		List<SshPubKeyRegistryEntity> regKeyList = sshPubKeyRegistryService.findByRegistry(registry.getId());
		
		StringBuffer sb = new StringBuffer();
		for (SshPubKeyRegistryEntity regKey : regKeyList) {
			if (regKey.getSshPubKey().getKeyStatus().equals(SshPubKeyStatus.ACTIVE) &&
					regKey.getKeyStatus().equals(SshPubKeyRegistryStatus.ACTIVE) &&
					(regKey.getExpiresAt() == null || (System.currentTimeMillis() - regKey.getExpiresAt().getTime()) < 0) &&
					(regKey.getSshPubKey().getExpiresAt() == null || (System.currentTimeMillis() - regKey.getSshPubKey().getExpiresAt().getTime()) < 0)
					) {
				if (regKey.getUsageType().equals(SshPubKeyUsageType.COMMAND)) {
					sb.append("command=\"");
					sb.append(regKey.getCommand());
					sb.append("\" from=\"");
					sb.append(regKey.getFrom());
					sb.append("\" ");
				}
				sb.append(regKey.getSshPubKey().getKeyType());
				sb.append(" ");
				sb.append(regKey.getSshPubKey().getEncodedKey());
				if (user.getEmail() != null) {
					sb.append(" ");
					sb.append(user.getEmail());
				}
				sb.append("\n");
			}
		}
		return sb.toString();
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

		UserEntity user = userService.findByUidNumber(uidNumber);
		if (user == null)
			throw new NoUserFoundException("No such user");
		
		logger.debug("Searching for active registry for user {} and service {}", user.getId(), service.getShortName());
		RegistryEntity registry = registryService.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
		
		if (registry == null)
			throw new NoRegistryFoundException("No active registry for user");
		
		List<SshPubKeyRegistryEntity> regKeyList = sshPubKeyRegistryService.findByRegistry(registry.getId());
		
		StringBuffer sb = new StringBuffer();
		for (SshPubKeyRegistryEntity regKey : regKeyList) {
			if (regKey.getSshPubKey().getKeyStatus().equals(SshPubKeyStatus.ACTIVE) &&
					regKey.getUsageType().equals(SshPubKeyUsageType.INTERACTIVE) &&
					regKey.getKeyStatus().equals(SshPubKeyRegistryStatus.ACTIVE) &&
					(regKey.getExpiresAt() == null || (System.currentTimeMillis() - regKey.getExpiresAt().getTime()) < 0) &&
					(regKey.getSshPubKey().getExpiresAt() == null || (System.currentTimeMillis() - regKey.getSshPubKey().getExpiresAt().getTime()) < 0)
					) {
				sb.append(regKey.getSshPubKey().getKeyType());
				sb.append(" ");
				sb.append(regKey.getSshPubKey().getEncodedKey());
				if (user.getEmail() != null) {
					sb.append(" ");
					sb.append(user.getEmail());
				}
				sb.append("\n");
			}
		}
		return sb.toString();
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

		UserEntity user = userService.findByUidNumber(uidNumber);
		if (user == null)
			throw new NoUserFoundException("No such user");
		
		logger.debug("Searching for active registry for user {} and service {}", user.getId(), service.getShortName());
		RegistryEntity registry = registryService.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
		
		if (registry == null)
			throw new NoRegistryFoundException("No active registry for user");
		
		List<SshPubKeyRegistryEntity> regKeyList = sshPubKeyRegistryService.findByRegistry(registry.getId());
		
		StringBuffer sb = new StringBuffer();
		for (SshPubKeyRegistryEntity regKey : regKeyList) {
			if (regKey.getSshPubKey().getKeyStatus().equals(SshPubKeyStatus.ACTIVE) &&
					regKey.getUsageType().equals(SshPubKeyUsageType.COMMAND) &&
					regKey.getKeyStatus().equals(SshPubKeyRegistryStatus.ACTIVE) &&
					(regKey.getExpiresAt() == null || (System.currentTimeMillis() - regKey.getExpiresAt().getTime()) < 0) &&
					(regKey.getSshPubKey().getExpiresAt() == null || (System.currentTimeMillis() - regKey.getSshPubKey().getExpiresAt().getTime()) < 0)
					) {
				sb.append("command=\"");
				sb.append(regKey.getCommand());
				sb.append("\" from=\"");
				sb.append(regKey.getFrom());
				sb.append("\" ");
				sb.append(regKey.getSshPubKey().getKeyType());
				sb.append(" ");
				sb.append(regKey.getSshPubKey().getEncodedKey());
				if (user.getEmail() != null) {
					sb.append(" ");
					sb.append(user.getEmail());
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	protected Boolean checkAccess(HttpServletRequest request, String roleName) {
		Boolean check;
		
		if (request.getAttribute(SecurityFilter.USER_ID) != null &&
				request.getAttribute(SecurityFilter.USER_ID) instanceof Long) {
			Long userId = (Long) request.getAttribute(SecurityFilter.USER_ID);
			check = roleService.checkUserInRole(userId, roleName);
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
		if (request.getAttribute(SecurityFilter.USER_ID) != null &&
				request.getAttribute(SecurityFilter.USER_ID) instanceof Long) {
			Long userId = (Long) request.getAttribute(SecurityFilter.USER_ID);
			return "user-" + userId;
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
