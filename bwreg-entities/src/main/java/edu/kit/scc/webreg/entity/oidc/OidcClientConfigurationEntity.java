package edu.kit.scc.webreg.entity.oidc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;

@Entity(name = "OidcClientConfigurationEntity")
@Table(name = "oidc_client_configuration")
public class OidcClientConfigurationEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", length = 64)
	private String name;

	@Column(name = "secret", length = 256)
	private String secret;

	@Column(name = "displayName", length = 1024)
	private String displayName;

	@ManyToOne(targetEntity = OidcOpConfigurationEntity.class)
	private OidcOpConfigurationEntity opConfiguration;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public OidcOpConfigurationEntity getOpConfiguration() {
		return opConfiguration;
	}

	public void setOpConfiguration(OidcOpConfigurationEntity opConfiguration) {
		this.opConfiguration = opConfiguration;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
