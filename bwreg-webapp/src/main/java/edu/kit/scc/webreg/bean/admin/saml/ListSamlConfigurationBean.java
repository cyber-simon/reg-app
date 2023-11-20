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

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;

@Named
@ViewScoped
public class ListSamlConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<SamlSpConfigurationEntity> spList;

    @Inject
    private SamlSpConfigurationService spService;

	public List<SamlSpConfigurationEntity> getSpList() {
		if (spList == null) {
			spList = spService.findAll();
		}
		return spList;
	}
}
