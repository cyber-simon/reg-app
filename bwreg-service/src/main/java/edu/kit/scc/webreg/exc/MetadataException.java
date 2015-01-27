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

public class MetadataException extends Exception {

	private static final long serialVersionUID = 1L;

	public MetadataException() {
	}

	public MetadataException(String arg0) {
		super(arg0);
	}

	public MetadataException(Throwable arg0) {
		super(arg0);
	}

	public MetadataException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
