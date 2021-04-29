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
package edu.kit.scc.webreg.bean.admin.group;

import java.io.Serializable;

import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.LocalGroupService;

@Named
@ViewScoped
public class ListLocalGroupBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LazyDataModel<LocalGroupEntity> list;
    
    @Inject
    private LocalGroupService service;

	public void preRenderView(ComponentSystemEvent ev) {
		if (list == null) {
			list = new GenericLazyDataModelImpl<LocalGroupEntity, LocalGroupService, Long>(service);
		}
	}

    public LazyDataModel<LocalGroupEntity> getGroupEntityList() {
   		return list;
    }

}
