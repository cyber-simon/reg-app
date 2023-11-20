package edu.kit.scc.webreg.entity.oidc;

import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

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

	@ElementCollection
	@JoinTable(name = "oidc_client_generic_store")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> genericStore; 

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

	public Map<String, String> getGenericStore() {
		return genericStore;
	}

	public void setGenericStore(Map<String, String> genericStore) {
		this.genericStore = genericStore;
	}
}
