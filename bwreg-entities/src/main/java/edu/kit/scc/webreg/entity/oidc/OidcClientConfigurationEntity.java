package edu.kit.scc.webreg.entity.oidc;

import java.util.Map;
import java.util.Set;

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

	@ElementCollection
	@JoinTable(name = "oidc_client_redirects")
    @Column(name = "value_data", length = 2048)
    private Set<String> redirects;

	public Map<String, String> getGenericStore() {
		return genericStore;
	}

	public void setGenericStore(Map<String, String> genericStore) {
		this.genericStore = genericStore;
	}

	public Set<String> getRedirects() {
		return redirects;
	}

	public void setRedirects(Set<String> redirects) {
		this.redirects = redirects;
	}
}
