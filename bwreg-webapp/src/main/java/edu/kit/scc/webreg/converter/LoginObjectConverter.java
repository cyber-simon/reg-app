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
package edu.kit.scc.webreg.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.oidc.OidcRpConfigurationService;

@Named("loginObjectConverter")
public class LoginObjectConverter implements Converter, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlIdpMetadataService idpService;

	@Inject
	private OidcRpConfigurationService rpService;

	@Override
	public Object getAsObject(FacesContext ctx, UIComponent component, String value)
			throws ConverterException {
        if (value == null || value.length() < 3) {
            return null;
        }
        Long id = Long.decode(value.substring(2));
        Object o;
        if (value.startsWith("i_")) {
            o = idpService.findById(id);
        }
        else {
            o = rpService.findById(id);
        }
		return o;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getAsString(FacesContext ctx, UIComponent component, Object value)
			throws ConverterException {
        if (value == null) {
            return "";
        }
        if (value instanceof SamlIdpMetadataEntity) {
            return "i_" + ((BaseEntity<Long>)value).getId().toString();
        }
        else {
            return "o_" + ((BaseEntity<Long>)value).getId().toString();
        }
	}	
}
