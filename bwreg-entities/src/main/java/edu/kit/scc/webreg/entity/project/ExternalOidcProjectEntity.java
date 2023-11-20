package edu.kit.scc.webreg.entity.project;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;

@Entity(name = "ExternalOidcProjectEntity")
public class ExternalOidcProjectEntity extends ExternalProjectEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = OidcRpConfigurationEntity.class)
	private OidcRpConfigurationEntity rpConfig;

	public OidcRpConfigurationEntity getRpConfig() {
		return rpConfig;
	}

	public void setRpConfig(OidcRpConfigurationEntity rpConfig) {
		this.rpConfig = rpConfig;
	}
}
