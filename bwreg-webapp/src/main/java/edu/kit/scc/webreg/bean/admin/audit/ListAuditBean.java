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
package edu.kit.scc.webreg.bean.admin.audit;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.audit.AuditEntryService;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;

@ManagedBean
@ViewScoped
public class ListAuditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LazyDataModel<AuditEntryEntity> list;
    
    @Inject
    private AuditEntryService service;

	public void preRenderView(ComponentSystemEvent ev) {
		if (list == null) {
			list = new GenericLazyDataModelImpl<AuditEntryEntity, AuditEntryService, Long>(service);
		}
	}

    public LazyDataModel<AuditEntryEntity> getList() {
   		return list;
    }
}
