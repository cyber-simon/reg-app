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

import java.io.Serializable;

public class SamlAuthenticationException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	public SamlAuthenticationException(String msg) {
		super(msg);
	}

	public SamlAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}

}
