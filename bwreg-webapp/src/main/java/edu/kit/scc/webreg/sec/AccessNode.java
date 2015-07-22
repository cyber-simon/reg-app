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
package edu.kit.scc.webreg.sec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AccessNode {

	private String path;
	
	private AccessNode parent;
	
	private Map<String, AccessNode> children;
	
	private Set<Long> allowRoles;
	private Set<Long> denyRoles;
	
	public AccessNode() {
		this(null, "", false);
	}
	
	public AccessNode(AccessNode parent, String path, Boolean inherit) {
		this.parent = parent;
		this.path = path;
		children = new HashMap<String, AccessNode>();
		allowRoles = new HashSet<Long>();
		denyRoles = new HashSet<Long>();
		
		if (inherit) {
			allowRoles.addAll(parent.getAllowRoles());
			denyRoles.addAll(parent.getDenyRoles());
		}
		
		if (parent != null)
			parent.addChild(this);
	}

	public AccessNode getChild(String path) {
		return children.get(path);
	}
	
	public void addAllowRole(Long role) {
		allowRoles.add(role);
	}

	public void addDenyRole(Long role) {
		denyRoles.add(role);
	}
	
	public void addChild(AccessNode an) {
		if (an.getParent() != this)
			throw new IllegalArgumentException("Cannot add AccessNode Child. Wrong parent.");
		children.put(an.getPath(), an);
	}
	
	public Set<Long> getAllowRoles() {
		return allowRoles;
	}

	public Set<Long> getDenyRoles() {
		return denyRoles;
	}
	
	public AccessNode getParent() {
		return parent;
	}	

	public String getPath() {
		return path;
	}
}
