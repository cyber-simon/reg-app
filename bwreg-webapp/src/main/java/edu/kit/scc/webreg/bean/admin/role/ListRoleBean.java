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
package edu.kit.scc.webreg.bean.admin.role;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.RoleService;

@ManagedBean
@ViewScoped
public class ListRoleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LazyDataModel<RoleEntity> list;
    
    @Inject
    private RoleService service;

    public LazyDataModel<RoleEntity> getRoleEntityList() {
    	if (list == null) 
    		list = new GenericLazyDataModelImpl<RoleEntity, RoleService, Long>(service);
        return list;
    }

}
