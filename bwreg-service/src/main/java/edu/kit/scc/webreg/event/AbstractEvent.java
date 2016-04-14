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

import edu.kit.scc.webreg.audit.Auditor;

public class AbstractEvent<E extends Serializable> implements Event<E> {

	private static final long serialVersionUID = 1L;

	private E entity;
	
	private Auditor auditor;

	public AbstractEvent(E entity, Auditor auditor) {
		this.entity = entity;
		this.auditor = auditor;
	}

	public AbstractEvent(E entity) {
		this(entity, null);
	}

	public E getEntity() {
		return entity;
	}

	public Auditor getAuditor() {
		return auditor;
	}
		
}
