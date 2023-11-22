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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.service.BaseService;

public abstract class AbstractConverter<T extends BaseEntity> implements Converter<T>, Serializable {

	private static final long serialVersionUID = 1L;

	protected abstract BaseService<? extends BaseEntity> getService();
	
	@SuppressWarnings("unchecked")
	@Override
	public T getAsObject(FacesContext ctx, UIComponent component, String value)
			throws ConverterException {
        if (value == null || value.length() == 0) {
            return null;
        }
        Long id = Long.decode(value);
        T o = (T) getService().fetch(id);
		return o;
	}

	@Override
	public String getAsString(FacesContext ctx, UIComponent component, T value)
			throws ConverterException {
        if (value == null) {
            return "";
        }
        return value.getId().toString();
	}	
}
