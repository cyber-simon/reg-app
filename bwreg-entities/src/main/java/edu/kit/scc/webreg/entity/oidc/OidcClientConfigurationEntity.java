package edu.kit.scc.webreg.entity.oidc;

import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity(name = "OidcClientConfigurationEntity")
@Table(name = "oidc_client_configuration")
public class OidcClientConfigurationEntity extends OidcClientConsumerEntity {

	private static final long serialVersionUID = 1L;

	@ElementCollection
	@JoinTable(name = "oidc_client_generic_store")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> genericStore; 

	public Map<String, String> getGenericStore() {
		return genericStore;
	}

	public void setGenericStore(Map<String, String> genericStore) {
		this.genericStore = genericStore;
	}
}
