package edu.kit.scc.webreg.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity(name = "ExternalUserEntity")
public class ExternalUserEntity extends UserEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "external_id", length = 1024)
	private String externalId;
	
	@ManyToOne(targetEntity = ExternalUserAdminRoleEntity.class)
	private ExternalUserAdminRoleEntity admin;

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public ExternalUserAdminRoleEntity getAdmin() {
		return admin;
	}

	public void setAdmin(ExternalUserAdminRoleEntity admin) {
		this.admin = admin;
	}	
}
