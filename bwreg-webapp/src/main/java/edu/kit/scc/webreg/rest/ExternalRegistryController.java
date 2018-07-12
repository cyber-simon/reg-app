package edu.kit.scc.webreg.rest;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dto.entity.RegistryEntityDto;
import edu.kit.scc.webreg.dto.service.RegistryDtoService;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.service.reg.RegisterUserService;

@Path("/external-reg")
public class ExternalRegistryController {

	@Inject
	private Logger logger;
	
	@Inject
	private RegisterUserService registerUserService;
	
	@Inject
	private RegistryDtoService registryDtoService;
	
	@Path(value = "/register/externalId/{externalId}/ssn/{ssn}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public RegistryEntityDto registerExternalUser(@PathParam("externalId") String externalId,
			@PathParam("ssn") String ssn, @Context HttpServletRequest request)
			throws IOException, RestInterfaceException, ServletException {
		RegistryEntity registry;
		try {
			registry = registerUserService.registerUser(externalId, ssn, "external", true, null);
			return registryDtoService.findById(registry.getId());
		} catch (RegisterException e) {
			logger.info("Exception", e);
			return null;
		}
	}

	@Path(value = "/find/externalId/{externalId}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public List<RegistryEntityDto> find(@PathParam("externalId") String externalId, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		return registryDtoService.findByExternalId(externalId);
	}


}
