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

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.JobScheduleEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.JobScheduleService;

@Named
@ViewScoped
public class ListJobScheduleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LazyDataModel<JobScheduleEntity> list;
    
    @Inject
    private JobScheduleService service;

    public LazyDataModel<JobScheduleEntity> getList() {
		if (list == null) {
			list = new GenericLazyDataModelImpl<JobScheduleEntity, JobScheduleService>(service);
		}
   		return list;
    }
    
    public void delete(JobScheduleEntity entity) {
    	service.delete(entity);
    }
}
