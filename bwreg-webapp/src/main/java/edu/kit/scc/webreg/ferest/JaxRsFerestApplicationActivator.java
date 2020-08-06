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
package edu.kit.scc.webreg.ferest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/ferest")
public class JaxRsFerestApplicationActivator extends Application {

	   @Override
	    public Set<Class<?>> getClasses() {
	        Set<Class<?>> resources = new HashSet<>();

	        resources.add(IdpListController.class);
	        resources.add(FederationListController.class);
	        
	        return resources;
	    }
}
