package edu.kit.scc.webreg.entity.oidc;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Entity(name = "OidcFlowStateEntity")
@Table(name = "oidc_flow_state")
public class OidcFlowStateEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(targetEntity = UserEntity.class)
	private UserEntity user;

	@ManyToOne(targetEntity = OidcOpConfigurationEntity.class)
	private OidcOpConfigurationEntity opConfiguration;

	@ManyToOne(targetEntity = OidcClientConfigurationEntity.class)
	private OidcClientConfigurationEntity clientConfiguration;

	@Column(name = "nonce", length = 256)
	private String nonce;

	@Column(name = "state", length = 256)
	private String state;

	@Column(name = "code", length = 256)
	private String code;

	@Column(name = "response_type", length = 256)
	private String responseType;

	@Column(name = "redirect_uri", length = 1024)
	private String redirectUri;
	
	@Column(name = "access_token", length = 256)
	private String accessToken;

	@Column(name = "access_token_type", length = 32)
	private String accessTokenType;

	@Column(name = "valid_until")
	private Date validUntil;
	
	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getResponseType() {
		return responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessTokenType() {
		return accessTokenType;
	}

	public void setAccessTokenType(String accessTokenType) {
		this.accessTokenType = accessTokenType;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public OidcOpConfigurationEntity getOpConfiguration() {
		return opConfiguration;
	}

	public void setOpConfiguration(OidcOpConfigurationEntity opConfiguration) {
		this.opConfiguration = opConfiguration;
	}

	public OidcClientConfigurationEntity getClientConfiguration() {
		return clientConfiguration;
	}

	public void setClientConfiguration(OidcClientConfigurationEntity clientConfiguration) {
		this.clientConfiguration = clientConfiguration;
	}
}
