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
package edu.kit.scc.webreg.bean.admin.saml;

import java.io.Serializable;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.service.FederationService;

@Named("listFederationBean")
@RequestScoped
public class ListFederationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<FederationEntity> list;
    
    @Inject
    private FederationService service;

    @PostConstruct
    public void init() {
		list = service.findAll();
	}
	
    public List<FederationEntity> getEntityList() {
   		return list;
    }

}
