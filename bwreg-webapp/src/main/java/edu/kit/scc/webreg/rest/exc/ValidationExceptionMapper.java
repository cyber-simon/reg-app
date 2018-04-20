package edu.kit.scc.webreg.rest.exc;

import javax.validation.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
	
	@Override
	public Response toResponse(ValidationException ex) {
		return Response.status(500).entity(ex.getMessage())
				.type(MediaType.TEXT_PLAIN).build();
	}
	
}
