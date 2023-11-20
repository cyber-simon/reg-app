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
package edu.kit.scc.webreg.bean.admin.oidc;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.oidc.OidcClientConfigurationService;

@Named
@ViewScoped
public class ListOidcClientConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LazyDataModel<OidcClientConfigurationEntity> list;
    
    @Inject
    private OidcClientConfigurationService service;

    @PostConstruct
    public void init() {
	}

	public LazyDataModel<OidcClientConfigurationEntity> getList() {
		if (list == null)
			list = new GenericLazyDataModelImpl<OidcClientConfigurationEntity, OidcClientConfigurationService>(service);
   		return list;
   	}
}
