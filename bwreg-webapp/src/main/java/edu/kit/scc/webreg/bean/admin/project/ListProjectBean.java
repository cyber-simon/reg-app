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
package edu.kit.scc.webreg.bean.admin.project;

import java.io.Serializable;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.project.ProjectService;

@Named
@ViewScoped
public class ListProjectBean implements Serializable {

	private static final long serialVersionUID = 1L;

    @Inject
    private Logger logger;
    
    @Inject
    private ProjectService service;

	private LazyDataModel<ProjectEntity> list;

	public void preRenderView(ComponentSystemEvent ev) {
		if (list == null) {
			list = new GenericLazyDataModelImpl<ProjectEntity, ProjectService>(service);
		}
	}

    public LazyDataModel<ProjectEntity> getProjectEntityList() {
   		return list;
    }
}
