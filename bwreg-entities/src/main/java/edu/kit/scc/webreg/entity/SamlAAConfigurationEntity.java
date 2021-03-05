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
package edu.kit.scc.webreg.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "aaconfig")
public class SamlAAConfigurationEntity extends SamlConfigurationEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "aq", length = 2048)
	private String aq;
	
	@ElementCollection
	private List<String> hostNameList = new ArrayList<String>();
	
	public List<String> getHostNameList() {
		return hostNameList;
	}

	public void setHostNameList(List<String> hostNameList) {
		this.hostNameList = hostNameList;
	}

	public String getAq() {
		return aq;
	}

	public void setAq(String aq) {
		this.aq = aq;
	}
}
