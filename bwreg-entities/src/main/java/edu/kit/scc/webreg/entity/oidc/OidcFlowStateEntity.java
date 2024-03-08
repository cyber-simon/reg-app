package edu.kit.scc.webreg.entity.oidc;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@Entity(name = "OidcFlowStateEntity")
@Table(name = "oidc_flow_state")
public class OidcFlowStateEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(targetEntity = UserEntity.class)
	private UserEntity user;

	@ManyToOne(targetEntity = IdentityEntity.class)
	private IdentityEntity identity;

	@ManyToOne(targetEntity = OidcOpConfigurationEntity.class)
	private OidcOpConfigurationEntity opConfiguration;

	@ManyToOne(targetEntity = OidcClientConfigurationEntity.class)
	private OidcClientConfigurationEntity clientConfiguration;

	@ManyToOne(targetEntity = OidcClientConsumerEntity.class)
	private OidcClientConsumerEntity clientConsumer;

	@ManyToOne(targetEntity = RegistryEntity.class)
	private RegistryEntity registry;

	@ManyToOne(targetEntity = AttributeReleaseEntity.class)
	private AttributeReleaseEntity attributeRelease;

	@Column(name = "nonce", length = 256)
	private String nonce;

	@Column(name = "state", length = 256)
	private String state;

	@Column(name = "code", length = 256)
	private String code;

	@Column(name = "code_challange", length = 512)
	private String codeChallange;

	@Column(name = "code_challange_method", length = 64)
	private String codecodeChallangeMethod;

	@Column(name = "response_type", length = 256)
	private String responseType;

	@Column(name = "redirect_uri", length = 1024)
	private String redirectUri;
	
	@Column(name = "access_token", length = 4096)
	private String accessToken;

	@Column(name = "refresh_token", length = 4096)
	private String refreshToken;

	@Column(name = "scope", length = 4096)
	private String scope;

	@Column(name = "acr_values", length = 4096)
	private String acrValues;

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

	public RegistryEntity getRegistry() {
		return registry;
	}

	public void setRegistry(RegistryEntity registry) {
		this.registry = registry;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getCodeChallange() {
		return codeChallange;
	}

	public void setCodeChallange(String codeChallange) {
		this.codeChallange = codeChallange;
	}

	public String getCodecodeChallangeMethod() {
		return codecodeChallangeMethod;
	}

	public void setCodecodeChallangeMethod(String codecodeChallangeMethod) {
		this.codecodeChallangeMethod = codecodeChallangeMethod;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getAcrValues() {
		return acrValues;
	}

	public void setAcrValues(String acrValues) {
		this.acrValues = acrValues;
	}

	public AttributeReleaseEntity getAttributeRelease() {
		return attributeRelease;
	}

	public void setAttributeRelease(AttributeReleaseEntity attributeRelease) {
		this.attributeRelease = attributeRelease;
	}

	public OidcClientConsumerEntity getClientConsumer() {
		return clientConsumer;
	}

	public void setClientConsumer(OidcClientConsumerEntity clientConsumer) {
		this.clientConsumer = clientConsumer;
	}
}
