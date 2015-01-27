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
package edu.kit.scc.webreg.service.reg.ldap;

import java.util.Map;

import edu.kit.scc.webreg.exc.RegisterException;

public class PropertyReader {
	
	private Map<String, String> serviceProps;
	
	public PropertyReader(Map<String, String> serviceProps) throws RegisterException {
		this.serviceProps = serviceProps;

		if (serviceProps == null || serviceProps.isEmpty()) {
			throw new RegisterException("Service is not configured properly");
		}		
	}

	public String readProp(String key) throws RegisterException {
		if (serviceProps.containsKey(key))
			return serviceProps.get(key);
		else
			throw new RegisterException("Service is not configured properly. " + key + " is missing in Service Properties");		
	}
	
	public boolean hasProp(String key) {
		return serviceProps.containsKey(key);
	}
}
