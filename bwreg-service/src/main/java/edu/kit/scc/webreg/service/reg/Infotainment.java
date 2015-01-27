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
package edu.kit.scc.webreg.service.reg;

import java.io.Serializable;

public class Infotainment implements Serializable {

	private static final long serialVersionUID = 1L;

	private String message;
	
	private InfotainmentTreeNode root;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public InfotainmentTreeNode getRoot() {
		return root;
	}

	public void setRoot(InfotainmentTreeNode root) {
		this.root = root;
	}
}
