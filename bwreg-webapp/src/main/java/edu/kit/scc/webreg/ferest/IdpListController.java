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

import edu.kit.scc.webreg.dto.entity.ferest.IdpEntityDto;
import edu.kit.scc.webreg.dto.service.ferest.IdpDtoService;

@Path("/idp-list")
public class IdpListController {

	@Inject
	private Logger logger;
	
	@Inject
	private IdpDtoService service;
	
	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<IdpEntityDto> idpListAll(@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws Exception {

		logger.debug("idpListAll called");

		return service.findAll();
	}
	
}
