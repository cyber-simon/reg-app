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

import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.EmailTemplateEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.EmailTemplateService;

@Named
@ViewScoped
public class ListEmailTemplateBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LazyDataModel<EmailTemplateEntity> list;
    
    @Inject
    private EmailTemplateService service;
	
    public LazyDataModel<EmailTemplateEntity> getServiceEntityList() {
   		return list;
    }

	public LazyDataModel<EmailTemplateEntity> getList() {
    	if (list == null) 
    		list = new GenericLazyDataModelImpl<EmailTemplateEntity, EmailTemplateService>(service);
		return list;
	}
}
