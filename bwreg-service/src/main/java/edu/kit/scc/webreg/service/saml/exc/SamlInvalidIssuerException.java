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
package edu.kit.scc.webreg.service.saml.exc;

import java.io.Serializable;

public class SamlInvalidIssuerException extends SamlAuthenticationException implements Serializable {

	private static final long serialVersionUID = 1L;

	public SamlInvalidIssuerException(String msg) {
		super(msg);
	}

	public SamlInvalidIssuerException(String msg, Throwable t) {
		super(msg, t);
	}

}
