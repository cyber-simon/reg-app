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
@Table(name = "spconfig")
public class SamlSpConfigurationEntity extends SamlConfigurationEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "acs", length = 2048)
	private String acs;
	
	@Column(name = "ecp", length = 2048)
	private String ecp;
	
	@ElementCollection
	private List<String> hostNameList = new ArrayList<String>();
	
	public List<String> getHostNameList() {
		return hostNameList;
	}

	public void setHostNameList(List<String> hostNameList) {
		this.hostNameList = hostNameList;
	}

	public String getAcs() {
		return acs;
	}

	public void setAcs(String acs) {
		this.acs = acs;
	}

	public String getEcp() {
		return ecp;
	}

	public void setEcp(String ecp) {
		this.ecp = ecp;
	}
}
