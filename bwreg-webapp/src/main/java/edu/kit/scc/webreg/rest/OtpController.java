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
package edu.kit.scc.webreg.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.TwoFaLoginService;

@Path("/otp")
public class OtpController {

	@Inject
	private TwoFaLoginService twofaLoginService;

	@Path("/simplecheck/{service}/{secret}")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public String otpLoginPost(@PathParam("secret") String secret, @FormParam("user") String eppn,
			@PathParam("service") String serviceShortName, @FormParam("pass") String otp,
			@Context HttpServletRequest request) throws IOException, ServletException, RestInterfaceException {

		try {
			return twofaLoginService.otpLogin(eppn, serviceShortName, otp, secret, request, true);
		} catch (TwoFaException | RestInterfaceException e) {
			return ":-(";
		}
	}

	@Path("/simplecheck/{service}/{secret}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String otpLoginGet(@PathParam("secret") String secret, @QueryParam("user") String eppn,
			@PathParam("service") String serviceShortName, @QueryParam("pass") String otp,
			@Context HttpServletRequest request) throws IOException, ServletException, RestInterfaceException {

		try {
			return twofaLoginService.otpLogin(eppn, serviceShortName, otp, secret, request, true);
		} catch (TwoFaException | RestInterfaceException e) {
			return ":-(";
		}
	}

	@Path("/simplecheck-global/{service}/{secret}")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public String otpLoginGlobalPost(@PathParam("secret") String secret, @FormParam("user") String eppn,
			@PathParam("service") String serviceShortName, @FormParam("pass") String otp,
			@Context HttpServletRequest request) throws IOException, ServletException, RestInterfaceException {

		try {
			return twofaLoginService.otpLogin(eppn, serviceShortName, otp, secret, request, false);
		} catch (TwoFaException | RestInterfaceException e) {
			return ":-(";
		}
	}

	@Path("/simplecheck-global/{service}/{secret}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String otpLoginGlobalGet(@PathParam("secret") String secret, @QueryParam("user") String eppn,
			@PathParam("service") String serviceShortName, @QueryParam("pass") String otp,
			@Context HttpServletRequest request) throws IOException, ServletException, RestInterfaceException {

		try {
			return twofaLoginService.otpLogin(eppn, serviceShortName, otp, secret, request, false);
		} catch (TwoFaException | RestInterfaceException e) {
			return ":-(";
		}
	}

}
