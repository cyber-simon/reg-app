package edu.kit.scc.webreg.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.dto.service.ExternalUserDtoService;
import edu.kit.scc.webreg.exc.RestInterfaceException;

@Path("/external-user")
public class ExternalUserController {

	@Inject 
	private Logger logger;
	
	@Inject
	private ExternalUserDtoService externalUserDtoService;
	
	@Path(value = "/create")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@POST
	public Map<String, String> create(@Valid ExternalUserEntityDto externalUserDto, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {

		logger.info("Create external user called for {}", externalUserDto.getExternalId());
		
		externalUserDtoService.createExternalUser(externalUserDto);
		
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
		
		externalUserDtoService.updateExternalUser(externalUserDto);
		
		Map<String, String> map = new HashMap<>();
		map.put("result", "success");
		return map;
	}

	@Path(value = "/find/externalId/{externalId}")
	@Produces({MediaType.APPLICATION_JSON})
	@GET
	public ExternalUserEntityDto updateUser(@PathParam("externalId") String externalId, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		return externalUserDtoService.findByExternalId(externalId);
	}
}
