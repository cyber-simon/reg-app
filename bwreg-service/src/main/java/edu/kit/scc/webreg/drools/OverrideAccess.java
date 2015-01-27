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
package edu.kit.scc.webreg.drools;

import java.io.Serializable;

import edu.kit.scc.webreg.entity.UserEntity;

public class OverrideAccess implements Serializable {

	private static final long serialVersionUID = 1L;

	private UserEntity user;
	
	public OverrideAccess(UserEntity user) {
		super();
		this.user = user;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}
}
