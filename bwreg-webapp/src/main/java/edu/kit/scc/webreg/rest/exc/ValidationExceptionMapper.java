package edu.kit.scc.webreg.rest.exc;

import java.util.List;

import javax.validation.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ResteasyViolationException;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
	
	@Override
	public Response toResponse(ValidationException ex) {
		if (ex instanceof ResteasyViolationException) {
			ResteasyViolationException e = (ResteasyViolationException) ex;
			StringBuilder strBuilder = new StringBuilder();
	        for (List<ResteasyConstraintViolation> cvl : e.getViolationLists()) {
	        	for (ResteasyConstraintViolation cv : cvl) {
	        		strBuilder.append(cv.getPath().toString() + ": " + cv.getMessage() + "\n");
	        	}
	        }
	        return Response
	                .status(Response.Status.NOT_ACCEPTABLE.getStatusCode())
	                .type(MediaType.TEXT_PLAIN)
	                .entity(strBuilder.toString())
	                .build();
		}
		else {
			return Response
                .status(Response.Status.NOT_ACCEPTABLE.getStatusCode())
                .type(MediaType.TEXT_PLAIN)
                .entity("Cannot validate Input: " + ex.getMessage())
                .build();
		}
	}
	
}
