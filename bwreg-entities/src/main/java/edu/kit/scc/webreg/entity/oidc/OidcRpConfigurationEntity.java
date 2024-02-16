package edu.kit.scc.webreg.entity.oidc;

import java.util.Map;

import edu.kit.scc.webreg.entity.UserProvisionerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity(name = "OidcRpConfigurationEntity")
@Table(name = "oidc_rp_configuration")
public class OidcRpConfigurationEntity extends UserProvisionerEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", length = 64)
	private String name;

	@Column(name = "client_id", length = 512)
	private String clientId;

	@Column(name = "scopes", length = 1024)
	private String scopes;

	@Column(name = "secret", length = 512)
	private String secret;

	@Column(name = "service_url", length = 1024)
	private String serviceUrl;

	@Column(name = "callback_url", length = 1024)
	private String callbackUrl;

	@ElementCollection
	@JoinTable(name = "oidc_rp_configuration_generic_store")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> genericStore; 

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getScopes() {
		return scopes;
	}

	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public Map<String, String> getGenericStore() {
		return genericStore;
	}

	public void setGenericStore(Map<String, String> genericStore) {
		this.genericStore = genericStore;
	}
}
