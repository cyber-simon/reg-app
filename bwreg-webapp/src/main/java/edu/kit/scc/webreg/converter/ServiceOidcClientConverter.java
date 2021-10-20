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

import edu.kit.scc.webreg.entity.oidc.ServiceOidcClientEntity;
import edu.kit.scc.webreg.service.BaseService;
import edu.kit.scc.webreg.service.oidc.ServiceOidcClientService;

@Named("serviceOidcClientConverter")
public class ServiceOidcClientConverter extends AbstractConverter<ServiceOidcClientEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceOidcClientService service;

	@Override
	protected BaseService<ServiceOidcClientEntity> getService() {
		return service;
	}
	
}
