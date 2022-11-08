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
package edu.kit.scc.webreg.bean.admin.service;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.ServiceService;

@Named
@ViewScoped
public class ListServiceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LazyDataModel<ServiceEntity> list;
    
    @Inject
    private ServiceService service;

    public LazyDataModel<ServiceEntity> getServiceEntityList() {
    	if (list == null) 
    		list = new GenericLazyDataModelImpl<ServiceEntity, ServiceService>(service);
   		return list;
    }

}
