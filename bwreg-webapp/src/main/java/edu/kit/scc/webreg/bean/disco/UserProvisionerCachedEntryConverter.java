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
package edu.kit.scc.webreg.bean.disco;

import java.io.Serializable;

import edu.kit.scc.webreg.service.disco.DiscoveryCacheService;
import edu.kit.scc.webreg.service.disco.UserProvisionerCachedEntry;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("userProvisionerCachedEntryConverter")
public class UserProvisionerCachedEntryConverter implements Converter<UserProvisionerCachedEntry>, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private DiscoveryCacheService discoveryCache;
	
	@Override
	public UserProvisionerCachedEntry getAsObject(FacesContext ctx, UIComponent component, String value)
			throws ConverterException {
        if (value == null || value.length() == 0) {
            return null;
        }
        Long id = Long.decode(value);
        UserProvisionerCachedEntry o = discoveryCache.getEntry(id);
		return o;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, UserProvisionerCachedEntry value) {
        if (value == null) {
            return "";
        }
        return value.getId().toString();
	}	

}
