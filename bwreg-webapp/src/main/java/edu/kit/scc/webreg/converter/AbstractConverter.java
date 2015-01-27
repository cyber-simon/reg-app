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

import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.service.BaseService;

public abstract class AbstractConverter implements Converter, Serializable {

	private static final long serialVersionUID = 1L;

	protected abstract BaseService<? extends BaseEntity<Long>, Long> getService();
	
	@Override
	public Object getAsObject(FacesContext ctx, UIComponent component, String value)
			throws ConverterException {
        if (value == null || value.length() == 0) {
            return null;
        }
        Long id = new Long(value);
        Object o = getService().findById(id);
		return o;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getAsString(FacesContext ctx, UIComponent component, Object value)
			throws ConverterException {
        if (value == null) {
            return "";
        }
        return ((BaseEntity<Long>)value).getId().toString();
	}	
}
