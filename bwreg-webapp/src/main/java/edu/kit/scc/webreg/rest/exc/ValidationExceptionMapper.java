package edu.kit.scc.webreg.rest.exc;

import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
	
	@Override
	public Response toResponse(ValidationException ex) {
		return Response
                .status(Response.Status.NOT_ACCEPTABLE.getStatusCode())
                .type(MediaType.TEXT_PLAIN)
                .entity("Cannot validate Input: " + ex.getMessage())
                .build();
	}
	
}
