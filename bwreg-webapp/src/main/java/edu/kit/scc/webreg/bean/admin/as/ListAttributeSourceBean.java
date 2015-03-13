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
package edu.kit.scc.webreg.bean.admin.as;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.AttributeSourceService;

@ManagedBean
@ViewScoped
public class ListAttributeSourceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LazyDataModel<AttributeSourceEntity> list;
    
    @Inject
    private AttributeSourceService service;

	public void preRenderView(ComponentSystemEvent ev) {
		if (list == null) {
			list = new GenericLazyDataModelImpl<AttributeSourceEntity, AttributeSourceService, Long>(service);
		}
	}

    public LazyDataModel<AttributeSourceEntity> getUserEntityList() {
   		return list;
    }

}
