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
package edu.kit.scc.webreg.bean.admin.timer;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.JobClassEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.JobClassService;

@Named
@ViewScoped
public class ListJobClassBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LazyDataModel<JobClassEntity> list;
    
    @Inject
    private JobClassService service;
	
    public LazyDataModel<JobClassEntity> getList() {
		if (list == null) {
			list = new GenericLazyDataModelImpl<JobClassEntity, JobClassService>(service, "schedules");
		}
   		return list;
    }
    
    public void delete(JobClassEntity entity) {
    	service.delete(entity);
    }
}
