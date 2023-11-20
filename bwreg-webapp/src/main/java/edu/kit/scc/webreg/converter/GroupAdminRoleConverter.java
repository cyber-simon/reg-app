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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.GroupAdminRoleEntity;
import edu.kit.scc.webreg.service.BaseService;
import edu.kit.scc.webreg.service.GroupAdminRoleService;

@Named("groupAdminRoleConverter")
public class GroupAdminRoleConverter extends AbstractConverter<GroupAdminRoleEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private GroupAdminRoleService service;

	@Override
	protected BaseService<GroupAdminRoleEntity> getService() {
		return service;
	}
	
}
