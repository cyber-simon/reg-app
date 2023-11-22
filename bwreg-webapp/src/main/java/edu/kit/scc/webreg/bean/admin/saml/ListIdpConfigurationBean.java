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

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity;
import edu.kit.scc.webreg.service.SamlIdpConfigurationService;
import edu.kit.scc.webreg.service.SamlSpMetadataService;
import edu.kit.scc.webreg.service.ServiceSamlSpService;

@Named
@ViewScoped
public class ListIdpConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<SamlIdpConfigurationEntity> idpList;
	private List<ServiceSamlSpEntity> serviceSpList;
	private List<SamlSpMetadataEntity> spList;
	
    @Inject
    private SamlIdpConfigurationService idpService;

    @Inject
    private ServiceSamlSpService serviceSamlSpService;

	@Inject
	private SamlSpMetadataService spService;

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

	public void delete(ServiceSamlSpEntity serviceSamlSpEntity) {
		serviceSamlSpService.delete(serviceSamlSpEntity);
		serviceSpList = null;
	}

	public List<SamlSpMetadataEntity> getSpList() {
		if (spList == null) {
			spList = spService.findAllByStatusOrderedByOrgname(SamlMetadataEntityStatus.ACTIVE);
		}
		return spList;
	}	
}
