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

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity;
import edu.kit.scc.webreg.service.SamlIdpConfigurationService;
import edu.kit.scc.webreg.service.ServiceSamlSpService;

@Named
@ViewScoped
public class ListIdpConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<SamlIdpConfigurationEntity> idpList;
	private List<ServiceSamlSpEntity> serviceSpList;
    
    @Inject
    private SamlIdpConfigurationService idpService;

    @Inject
    private ServiceSamlSpService serviceSamlSpService;
    
	public List<SamlIdpConfigurationEntity> getIdpList() {
		if (idpList == null) {
			idpList = idpService.findAll();
		}
		return idpList;
	}

	public List<ServiceSamlSpEntity> getServiceSpList() {
		if (serviceSpList == null) {
			serviceSpList = serviceSamlSpService.findAll();
		}
		return serviceSpList;
	}

}
