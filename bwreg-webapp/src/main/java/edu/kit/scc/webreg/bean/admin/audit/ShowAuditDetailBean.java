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

import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.audit.AuditDetailService;
import edu.kit.scc.webreg.entity.audit.AuditDetailEntity;

@Named("showAuditDetailBean")
@RequestScoped
public class ShowAuditDetailBean implements Serializable {

	private static final long serialVersionUID = 1L;

    @Inject
    private AuditDetailService service;

	private AuditDetailEntity entity;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.fetch(id);
	}
	
	public AuditDetailEntity getEntity() {
		return entity;
	}

	public void setEntity(AuditDetailEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
