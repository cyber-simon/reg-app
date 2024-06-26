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

import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity;

public class IdentityEmailAddressEvent extends AbstractEvent<IdentityEmailAddressEntity> {

	private static final long serialVersionUID = 1L;

	public IdentityEmailAddressEvent(IdentityEmailAddressEntity entity) {
		super(entity);
	}

	public IdentityEmailAddressEvent(IdentityEmailAddressEntity entity, AuditEntryEntity audit) {
		super(entity, audit);
	}
}
