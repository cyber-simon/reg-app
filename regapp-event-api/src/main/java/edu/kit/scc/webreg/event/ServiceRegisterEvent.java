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

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;

public class ServiceRegisterEvent extends AbstractEvent<RegistryEntity> {

	private static final long serialVersionUID = 1L;

	public ServiceRegisterEvent(RegistryEntity entity) {
		super(entity);
	}

	public ServiceRegisterEvent(RegistryEntity entity, AuditEntryEntity audit) {
		super(entity, audit);
	}
}
