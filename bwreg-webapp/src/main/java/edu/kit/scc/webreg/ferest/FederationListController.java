package edu.kit.scc.webreg.ferest;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

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
