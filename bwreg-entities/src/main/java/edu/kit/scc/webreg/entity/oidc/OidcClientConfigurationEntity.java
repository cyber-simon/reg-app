package edu.kit.scc.webreg.entity.oidc;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
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
