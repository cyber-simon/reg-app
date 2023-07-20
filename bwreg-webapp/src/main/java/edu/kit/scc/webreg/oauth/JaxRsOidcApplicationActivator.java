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
package edu.kit.scc.webreg.oauth;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/oidc")
public class JaxRsOidcApplicationActivator extends Application {

	   @Override
	    public Set<Class<?>> getClasses() {
	        Set<Class<?>> resources = new HashSet<>();
	        resources.add(OidcWellknownController.class);
	        resources.add(OidcAuthorizationController.class);
	        resources.add(OidcCertsController.class);
	        resources.add(OidcTokenController.class);
	        resources.add(OidcUserinfoController.class);
	        resources.add(OidcTokenIntrospectionController.class);
	        resources.add(JwtAuthController.class);
	        return resources;
	    }
}
