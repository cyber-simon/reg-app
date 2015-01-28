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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "spconfig")
public class SamlSpConfigurationEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "entity_id", length = 2048)
	private String entityId;
	
	@Enumerated(EnumType.STRING)
	private SamlMetadataEntityStatus status;
	
	@Column(name = "private_key")
	@Lob
	@Type(type = "org.hibernate.type.TextType")		
	private String privateKey;
	
	@Column(name = "certificate")
	@Lob
	@Type(type = "org.hibernate.type.TextType")		
	private String certificate;
	
	@Column(name = "acs", length = 2048)
	private String acs;
	
	@Column(name = "ecp", length = 2048)
	private String ecp;
	
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> hostNameList = new ArrayList<String>();
	
	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

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

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
}
