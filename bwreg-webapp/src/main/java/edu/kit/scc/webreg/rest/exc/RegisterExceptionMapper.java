/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.rest.exc;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import edu.kit.scc.webreg.exc.RegisterException;

@Provider
public class RegisterExceptionMapper implements ExceptionMapper<RegisterException> {

	@Override
	public Response toResponse(RegisterException ex) {
		return Response.status(500).entity(ex.getMessage())
				.type(MediaType.TEXT_PLAIN).build();
	}

}
