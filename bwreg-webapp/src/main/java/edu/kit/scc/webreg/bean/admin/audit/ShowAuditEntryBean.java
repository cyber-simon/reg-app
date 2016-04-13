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
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.audit.AuditDetailService;
import edu.kit.scc.webreg.audit.AuditEntryService;
import edu.kit.scc.webreg.entity.audit.AuditDetailEntity;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;

@Named("showAuditEntryBean")
@RequestScoped
public class ShowAuditEntryBean implements Serializable {

	private static final long serialVersionUID = 1L;

    @Inject
    private AuditEntryService service;

    @Inject
    private AuditDetailService auditDetailService;
    
	private AuditEntryEntity entity;
	
	private List<AuditDetailEntity> detailList;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.findById(id);
	}
	
	public AuditEntryEntity getEntity() {
		return entity;
	}

	public void setEntity(AuditEntryEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<AuditDetailEntity> getDetailList() {
		if (detailList == null)
			detailList = auditDetailService.findAllByAuditEntry(entity);
		return detailList;
	}
}
