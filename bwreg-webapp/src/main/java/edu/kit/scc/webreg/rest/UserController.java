package edu.kit.scc.webreg.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.service.UserUpdateService;

@Path("/user-admin")
public class UserController {

	@Inject
	private UserUpdateService userUpdateService;
	
	@Path(value = "/update-async/{eppn}")
	@Produces({"application/json"})
	@GET
	public Map<String, String> updateUserAsync(@PathParam("eppn") String eppn, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		
		userUpdateService.updateUserAsync(eppn, request.getLocalName(), "rest-/user-admin/update-async/" + eppn);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("reuqest", "submitted");
		return map;
	}

	@Path(value = "/update/{eppn}")
	@Produces({"application/json"})
	@GET
	public Map<String, String> updateUser(@PathParam("eppn") String eppn, @Context HttpServletRequest request)
					throws IOException, RestInterfaceException, ServletException {
		
		return userUpdateService.updateUser(eppn, request.getLocalName(), "rest-/user-admin/update/" + eppn);
	}	
}
