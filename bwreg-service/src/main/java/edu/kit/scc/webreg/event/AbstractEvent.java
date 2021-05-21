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
package edu.kit.scc.webreg.event;

import java.io.Serializable;

import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;

public class AbstractEvent<E extends Serializable> implements Event<E> {

	private static final long serialVersionUID = 1L;

	private E entity;
	
	private AuditEntryEntity audit;
	
	private String serverName;

	public AbstractEvent(E entity, AuditEntryEntity audit, String serverName) {
		this.entity = entity;
		this.audit = audit;
		this.serverName = serverName;
	}

	public AbstractEvent(E entity, AuditEntryEntity audit) {
		this(entity, audit, null);
	}

	public AbstractEvent(E entity) {
		this(entity, null, null);
	}

	public E getEntity() {
		return entity;
	}

	public AuditEntryEntity getAudit() {
		return audit;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
}
