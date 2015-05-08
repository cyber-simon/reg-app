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

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.RegisterException;

public class PropertyReader {
	
	private Map<String, String> serviceProps;
	
	public static PropertyReader newRegisterPropReader(ServiceEntity service) throws RegisterException {
		PropertyReader prop;
		try {
			prop = new PropertyReader(service.getServiceProps());
		} catch (PropertyReaderException e) {
			throw new RegisterException(e);
		}
		
		return prop;
	}
	
	public PropertyReader(Map<String, String> serviceProps) throws PropertyReaderException {
		this.serviceProps = serviceProps;

		if (serviceProps == null || serviceProps.isEmpty()) {
			throw new PropertyReaderException("Service is not configured properly");
		}		
	}

	public String readProp(String key) throws PropertyReaderException {
		if (serviceProps.containsKey(key))
			return readPropOrNull(key);
		else
			throw new PropertyReaderException("Service is not configured properly. " + key + " is missing in Service Properties");		
	}

	public String readPropOrNull(String key) {
		return serviceProps.get(key);
	}
	
	
	public boolean hasProp(String key) {
		return serviceProps.containsKey(key);
	}
}
