package edu.kit.scc.webreg.entity.oidc;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import edu.kit.scc.webreg.entity.UserEntity;

@Entity(name = "OidcUserEntity")
public class OidcUserEntity extends UserEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "subject_id", length = 1024)
	private String subjectId;
	
	@ManyToOne(targetEntity = OidcRpConfigurationEntity.class)
	private OidcRpConfigurationEntity issuer;

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public OidcRpConfigurationEntity getIssuer() {
		return issuer;
	}

	public void setIssuer(OidcRpConfigurationEntity issuer) {
		this.issuer = issuer;
	}
}
