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
package edu.kit.scc.webreg.converter;

import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.service.BaseService;
import edu.kit.scc.webreg.service.oidc.OidcOpConfigurationService;

@Named("oidcOpConfigurationConverter")
public class OidcOpConfigurationConverter extends AbstractConverter<OidcOpConfigurationEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcOpConfigurationService service;

	@Override
	protected BaseService<OidcOpConfigurationEntity, Long> getService() {
		return service;
	}
	
}
