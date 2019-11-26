package edu.kit.scc.webreg.entity.oidc;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;

@Entity(name = "OidcFlowStateEntity")
@Table(name = "oidc_flow_state")
public class OidcFlowStateEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "nonce", length = 256)
	private String nonce;

	@Column(name = "state", length = 256)
	private String state;

	@Column(name = "code", length = 256)
	private String code;

	@Column(name = "response_type", length = 256)
	private String responseType;

	@Column(name = "client_id", length = 256)
	private String clientId;

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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
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
}
