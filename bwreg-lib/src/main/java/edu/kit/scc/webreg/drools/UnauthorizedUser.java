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

public class UnauthorizedUser implements Serializable {

	private static final long serialVersionUID = 1L;

	private UserEntity user;
	
	private String message;

	public UnauthorizedUser(UserEntity user, String message) {
		super();
		this.user = user;
		this.message = message;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
