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
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.service.oidc.OidcOpConfigurationService;
import edu.kit.scc.webreg.service.oidc.OidcRpConfigurationService;

@Named("listOidcConfigurationBean")
@RequestScoped
public class ListOidcConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<OidcOpConfigurationEntity> opList;
	private List<OidcRpConfigurationEntity> rpList;
    
    @Inject
    private OidcOpConfigurationService opService;

    @Inject
    private OidcRpConfigurationService rpService;

    @PostConstruct
    public void init() {
		opList = opService.findAll();
		rpList = rpService.findAll();
	}

	public List<OidcOpConfigurationEntity> getOpList() {
		return opList;
	}

	public List<OidcRpConfigurationEntity> getRpList() {
		return rpList;
	}
}
