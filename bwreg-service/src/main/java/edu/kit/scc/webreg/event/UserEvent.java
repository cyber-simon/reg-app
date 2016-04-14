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

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.UserEntity;

public class UserEvent extends AbstractEvent<UserEntity> {


	private static final long serialVersionUID = 1L;

	public UserEvent(UserEntity entity) {
		super(entity);
	}

	public UserEvent(UserEntity entity, Auditor auditor) {
		super(entity, auditor);
	}
}
