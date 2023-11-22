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

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback=true)
public class RegisterException extends Exception {

	private static final long serialVersionUID = 1L;

	public RegisterException() {
		super();
	}

	public RegisterException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public RegisterException(String arg0) {
		super(arg0);
	}

	public RegisterException(Throwable arg0) {
		super(arg0);
	}

}
