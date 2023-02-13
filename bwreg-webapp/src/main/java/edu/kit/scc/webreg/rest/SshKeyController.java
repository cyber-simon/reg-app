package edu.kit.scc.webreg.rest;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.drools.exc.UnauthorizedException;
import edu.kit.scc.webreg.dto.entity.SshPubKeyEntityDto;
import edu.kit.scc.webreg.dto.service.SshPubKeyDtoService;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.exc.NoItemFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
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

	@Inject
	private RegistryDao registryDao;

	@Path(value = "/list/uidnumber/{uidNumber}/all")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	public List<SshPubKeyEntityDto> listKeysForUser(@PathParam("uidNumber") Integer uidNumber,
			@Context HttpServletRequest request) throws RestInterfaceException {
		return dtoService.findByUidNumber(uidNumber);
	}

	@Path(value = "/list/uidnumber/{uidNumber}/key-status/{status}")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	public List<SshPubKeyEntityDto> listKeysForUserAndStatus(@PathParam("uidNumber") Integer uidNumber,
			@PathParam("status") SshPubKeyStatus keyStatus, @Context HttpServletRequest request)
			throws RestInterfaceException {
		return dtoService.findByUidNumberAndStatus(uidNumber, keyStatus);
	}

	@Path(value = "/list/uidnumber/{uidNumber}/keys-expiring-soon/{days}")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	// example:
	// https://..../rest/ssh-key/list/uidnumber/900001/keys-expiring-soon/30
	public List<SshPubKeyEntityDto> listKeysForUserAndExpiryInDays(@PathParam("uidNumber") Integer uidNumber,
			@PathParam("days") Integer days, @Context HttpServletRequest request) throws RestInterfaceException {
		return dtoService.findByUidNumberAndExpiryInDays(uidNumber, days);
	}

	@Path(value = "/list/keys-expiring-soon/{days}")
	@Produces({ MediaType.APPLICATION_JSON })
	@GET
	// example: https://..../rest/ssh-key/list/keys-expiring-soon/30
	public List<SshPubKeyEntityDto> listKeysExpiryInDays(@PathParam("days") Integer days,
			@Context HttpServletRequest request) throws RestInterfaceException {
		return dtoService.findByExpiryInDays(days);
	}

	@Path(value = "/auth/all/{ssn}/uidnumber/{uidNumber}")
	@Produces({ MediaType.TEXT_PLAIN })
	@GET
	public String authByUidNumber(@PathParam("ssn") String ssn, @PathParam("uidNumber") Integer uidNumber,
			@Context HttpServletRequest request) throws RestInterfaceException {

		ServiceEntity service = serviceService.findByShortName(ssn);
		if (service == null)
			throw new NoItemFoundException("No such service");

		if (!checkAccess(request, service.getAdminRole().getName()))
			throw new UnauthorizedException("No access");

		return sshLoginService.authByUidNumber(service, uidNumber, request);
	}

	@Path(value = "/auth/interactive/{ssn}/uidnumber/{uidNumber}")
	@Produces({ MediaType.TEXT_PLAIN })
	@GET
	public String authByUidNumberInteractive(@PathParam("ssn") String ssn, @PathParam("uidNumber") Integer uidNumber,
			@Context HttpServletRequest request) throws RestInterfaceException {

		ServiceEntity service = serviceService.findByShortName(ssn);
		if (service == null)
			throw new NoItemFoundException("No such service");

		if (!checkAccess(request, service.getAdminRole().getName()))
			throw new UnauthorizedException("No access");

		return sshLoginService.authByUidNumberInteractive(service, uidNumber, request);
	}

	@Path(value = "/auth/command/{ssn}/uidnumber/{uidNumber}")
	@Produces({ MediaType.TEXT_PLAIN })
	@GET
	public String authByUidNumberCommand(@PathParam("ssn") String ssn, @PathParam("uidNumber") Integer uidNumber,
			@Context HttpServletRequest request) throws RestInterfaceException {

		ServiceEntity service = serviceService.findByShortName(ssn);
		if (service == null)
			throw new NoItemFoundException("No such service");

		if (!checkAccess(request, service.getAdminRole().getName()))
			throw new UnauthorizedException("No access");

		return sshLoginService.authByUidNumberCommand(service, uidNumber, request);
	}

	@Path(value = "/auth/all/{ssn}/localuid/{localuid}")
	@Produces({ MediaType.TEXT_PLAIN })
	@GET
	public String authByLocalUid(@PathParam("ssn") String ssn, @PathParam("localuid") String localUid,
			@Context HttpServletRequest request) throws RestInterfaceException {
		ServiceEntity service = serviceService.findByShortName(ssn);
		if (service == null)
			throw new NoItemFoundException("No such service");

		if (!checkAccess(request, service.getAdminRole().getName()))
			throw new UnauthorizedException("No access");

		Integer uidNumber = getUidNumberByLocalUid(service, localUid);
		return sshLoginService.authByUidNumber(service, uidNumber, request);
	}

	@Path(value = "/auth/interactive/{ssn}/localuid/{localuid}")
	@Produces({ MediaType.TEXT_PLAIN })
	@GET
	public String authByLocalUidInteractive(@PathParam("ssn") String ssn, @PathParam("localuid") String localUid,
			@Context HttpServletRequest request) throws RestInterfaceException {
		ServiceEntity service = serviceService.findByShortName(ssn);
		if (service == null)
			throw new NoItemFoundException("No such service");

		if (!checkAccess(request, service.getAdminRole().getName()))
			throw new UnauthorizedException("No access");

		Integer uidNumber = getUidNumberByLocalUid(service, localUid);
		return sshLoginService.authByUidNumberInteractive(service, uidNumber, request);
	}

	@Path(value = "/auth/command/{ssn}/localuid/{localuid}")
	@Produces({ MediaType.TEXT_PLAIN })
	@GET
	public String authByLocalUidCommand(@PathParam("ssn") String ssn, @PathParam("localuid") String localUid,
			@Context HttpServletRequest request) throws RestInterfaceException {
		ServiceEntity service = serviceService.findByShortName(ssn);
		if (service == null)
			throw new NoItemFoundException("No such service");

		if (!checkAccess(request, service.getAdminRole().getName()))
			throw new UnauthorizedException("No access");

		Integer uidNumber = getUidNumberByLocalUid(service, localUid);
		return sshLoginService.authByUidNumberCommand(service, uidNumber, request);
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

	protected Integer getUidNumberByLocalUid(ServiceEntity service, String localUid) throws NoUserFoundException {
		List<RegistryEntity> registryList = registryDao.findAllByRegValueAndStatus(service, "localUid", localUid,
				RegistryStatus.ACTIVE);
		if (registryList.size() == 0) {
			registryList.addAll(
					registryDao.findAllByRegValueAndStatus(service, "localUid", localUid, RegistryStatus.LOST_ACCESS));
		}
		if (registryList.size() == 0) {
			registryList.addAll(
					registryDao.findAllByRegValueAndStatus(service, "localUid", localUid, RegistryStatus.ON_HOLD));
		}
		if (registryList.size() == 0) {
			throw new NoUserFoundException("no such localUid in registries");
		}
		return registryList.get(0).getIdentity().getUidNumber();
	}

}
