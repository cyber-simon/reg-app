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

import java.util.HashMap;

import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;

public class TokenEvent extends AbstractEvent<HashMap<String, Object>> {

	private static final long serialVersionUID = 1L;

	public TokenEvent(HashMap<String, Object> entity) {
		super(entity);
	}

	public TokenEvent(HashMap<String, Object> entity, AuditEntryEntity audit) {
		super(entity, audit);
	}
}
