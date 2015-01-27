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
import java.util.ArrayList;
import java.util.List;

public class InfotainmentTreeNode implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nodeName;
	
	private String message;
	
	private InfotainmentTreeNode parent;
	private List<InfotainmentTreeNode> children;

	public InfotainmentTreeNode(String nodeName, String message, InfotainmentTreeNode parent) {
		this.nodeName = nodeName;
		this.message = message;
		this.parent = parent;
		if (parent != null)
			parent.addChild(this);
		children = new ArrayList<InfotainmentTreeNode>();
	}
	
	public InfotainmentTreeNode(String nodeName, InfotainmentTreeNode parent) {
		this(nodeName, null, parent);
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getMessage() {
		return message;
	}

	public InfotainmentTreeNode getParent() {
		return parent;
	}

	public List<InfotainmentTreeNode> getChildren() {
		return children;
	}
	
	public void addChild(InfotainmentTreeNode child) {
		children.add(child);
	}
}
