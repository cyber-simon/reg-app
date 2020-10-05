package edu.kit.scc.webreg.entity.oidc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;

@Entity(name = "OidcRpConfigurationEntity")
@Table(name = "oidc_rp_configuration")
public class OidcRpConfigurationEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", length = 64)
	private String name;

	@Column(name = "display_name", length = 256)
	private String displayName;

	@Column(name = "client_id", length = 512)
	private String clientId;

	@Column(name = "scopes", length = 1024)
	private String scopes;

	@Column(name = "secret", length = 512)
	private String secret;

	@Column(name = "service_url", length = 1024)
	private String serviceUrl;

	@Column(name = "auth_url", length = 1024)
	private String authUrl;

	@Column(name = "token_endpoint", length = 1024)
	private String tokenEndpoint;

	@Column(name = "userinfo_endpoint", length = 1024)
	private String userInfoEndpoint;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthUrl() {
		return authUrl;
	}

	public void setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
	}

	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

	public void setTokenEndpoint(String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}

	public String getUserInfoEndpoint() {
		return userInfoEndpoint;
	}

	public void setUserInfoEndpoint(String userInfoEndpoint) {
		this.userInfoEndpoint = userInfoEndpoint;
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
