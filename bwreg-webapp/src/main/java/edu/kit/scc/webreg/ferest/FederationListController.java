package edu.kit.scc.webreg.ferest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dto.entity.ferest.FederationEntityDto;
import edu.kit.scc.webreg.dto.service.ferest.FederationDtoService;

@Path("/federation-list")
public class FederationListController {

	@Inject
	private Logger logger;
	
	@Inject
	private FederationDtoService service;
	
	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FederationEntityDto> idpListAll(@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws Exception {

		logger.debug("federationListAll called");

		return service.findAll();
	}
	
}
