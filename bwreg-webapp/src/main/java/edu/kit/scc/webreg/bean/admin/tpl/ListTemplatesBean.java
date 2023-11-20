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
package edu.kit.scc.webreg.bean.admin.tpl;

import java.io.Serializable;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.VelocityTemplateEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.tpl.VelocityTemplateService;

@Named
@ViewScoped
public class ListTemplatesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LazyDataModel<VelocityTemplateEntity> list;
    
    @Inject
    private VelocityTemplateService service;

    public LazyDataModel<VelocityTemplateEntity> getEntityList() {
    	if (list == null) 
    		list = new GenericLazyDataModelImpl<VelocityTemplateEntity, VelocityTemplateService>(service);
   		return list;
    }

}
