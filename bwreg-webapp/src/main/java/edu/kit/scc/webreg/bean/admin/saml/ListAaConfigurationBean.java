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

import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import edu.kit.scc.webreg.service.SamlAAConfigurationService;

@Named
@ViewScoped
public class ListAaConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<SamlAAConfigurationEntity> aaList;

    @Inject
    private SamlAAConfigurationService aaService;

	public List<SamlAAConfigurationEntity> getAaList() {
		if (aaList == null) {
			aaList = aaService.findAll();
		}
		return aaList;
	}
}
