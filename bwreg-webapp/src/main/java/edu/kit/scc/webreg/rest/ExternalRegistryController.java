package edu.kit.scc.webreg.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dto.entity.RegistryEntityDto;
import edu.kit.scc.webreg.dto.service.RegistryDtoService;
import edu.kit.scc.webreg.exc.GenericRestInterfaceException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.RestInterfaceException;

@Path("/external-reg")
public class ExternalRegistryController {

	@Inject
	private Logger logger;
	
	@Inject
	private RegistryDtoService registryDtoService;
	
	@Path(value = "/register/externalId/{externalId}/ssn/{ssn}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public RegistryEntityDto registerExternalUser(@PathParam("externalId") String externalId,
			@PathParam("ssn") String ssn, @Context HttpServletRequest request)
			throws IOException, RestInterfaceException, ServletException {
		RegistryEntityDto registryDto;
		try {
			registryDto = registryDtoService.register(externalId, ssn);
			return registryDto;
		} catch (RegisterException e) {
			logger.info("Exception", e);
			return null;
		}
	}

	@Path(value = "/deregister/externalId/{externalId}/ssn/{ssn}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public Map<String, String>  deregisterExternalUser(@PathParam("externalId") String externalId,
			@PathParam("ssn") String ssn, @Context HttpServletRequest request)
			throws IOException, RestInterfaceException, ServletException {
		try {
			registryDtoService.deregister(externalId, ssn);
			Map<String, String> map = new HashMap<>();
			map.put("result", "success");
			return map;

		} catch (RegisterException e) {
			logger.info("Exception", e);
			throw new GenericRestInterfaceException("something went wrong: " + e.getMessage());
		}
	}

	@Path(value = "/find/externalId/{externalId}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public List<RegistryEntityDto> find(@PathParam("externalId") String externalId, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		return registryDtoService.findByExternalId(externalId);
	}

	@Path(value = "/find/all/ssn/{ssn}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public List<RegistryEntityDto> findAll(@PathParam("ssn") String ssn, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		return registryDtoService.findAllExternalBySsn(ssn);
	}


}
