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

import java.sql.Types;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;

@Entity(name="SamlConfigurationEntity")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class SamlConfigurationEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "entity_id", length = 2048)
	private String entityId;
	
	@Enumerated(EnumType.STRING)
	private SamlMetadataEntityStatus status;
	
	@Column(name = "private_key")
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)		
	private String privateKey;
	
	@Column(name = "certificate")
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)		
	private String certificate;
	
	@Column(name = "standby_private_key")
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)		
	private String standbyPrivateKey;
	
	@Column(name = "standby_certificate")
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)		
	private String standbyCertificate;
	
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

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public SamlMetadataEntityStatus getStatus() {
		return status;
	}

	public void setStatus(SamlMetadataEntityStatus status) {
		this.status = status;
	}

	public String getStandbyPrivateKey() {
		return standbyPrivateKey;
	}

	public void setStandbyPrivateKey(String standbyPrivateKey) {
		this.standbyPrivateKey = standbyPrivateKey;
	}

	public String getStandbyCertificate() {
		return standbyCertificate;
	}

	public void setStandbyCertificate(String standbyCertificate) {
		this.standbyCertificate = standbyCertificate;
	}
}
