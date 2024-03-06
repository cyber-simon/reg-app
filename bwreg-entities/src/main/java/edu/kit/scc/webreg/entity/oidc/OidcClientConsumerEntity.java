package edu.kit.scc.webreg.entity.oidc;

import edu.kit.scc.webreg.entity.attribute.AttributeConsumerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "OidcClientConsumerEntity")
@Table(name = "oidc_client_consumer")
public class OidcClientConsumerEntity extends AttributeConsumerEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", length = 64)
	private String name;

	@Column(name = "secret", length = 256)
	private String secret;

	@Column(name = "displayName", length = 1024)
	private String displayName;

	@ManyToOne(targetEntity = OidcOpConfigurationEntity.class)
	private OidcOpConfigurationEntity opConfiguration;

	@Column(name = "public_client")
	private Boolean publicClient;
	
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
	
	public Boolean getPublicClient() {
		return publicClient;
	}

	public void setPublicClient(Boolean publicClient) {
		this.publicClient = publicClient;
	}
}
