package edu.kit.scc.webreg.entity.oidc;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;

@Entity(name = "OidcGroupEntity")
public class OidcGroupEntity extends ServiceBasedGroupEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = OidcRpConfigurationEntity.class)
	private OidcRpConfigurationEntity issuer;

	@Column(name = "group_prefix")
	private String prefix;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public OidcRpConfigurationEntity getIssuer() {
		return issuer;
	}

	public void setIssuer(OidcRpConfigurationEntity issuer) {
		this.issuer = issuer;
	}
}
