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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.EmailTemplateEntity;
import edu.kit.scc.webreg.service.EmailTemplateService;

@Named("listEmailTemplateBean")
@RequestScoped
public class ListEmailTemplateBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<EmailTemplateEntity> list;
    
    @Inject
    private EmailTemplateService service;

    @PostConstruct
    public void init() {
		list = service.findAll();
	}
	
    public List<EmailTemplateEntity> getList() {
   		return list;
    }

}
