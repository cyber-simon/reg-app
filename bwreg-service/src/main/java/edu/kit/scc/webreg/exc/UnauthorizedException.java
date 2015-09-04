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
package edu.kit.scc.webreg.exc;

import java.util.List;

import edu.kit.scc.webreg.drools.UnauthorizedUser;

public class UnauthorizedException extends RestInterfaceException {

	private static final long serialVersionUID = 1L;

	private List<UnauthorizedUser> unauthList;
	
	public UnauthorizedException(List<UnauthorizedUser> unauthList) {
		super();
		
		this.unauthList = unauthList;
	}

	public UnauthorizedException(String message) {
		super(message);
	}

	public List<UnauthorizedUser> getUnauthList() {
		return unauthList;
	}

}
