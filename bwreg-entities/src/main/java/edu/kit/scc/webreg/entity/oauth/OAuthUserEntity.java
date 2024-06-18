package edu.kit.scc.webreg.entity.oauth;

import edu.kit.scc.webreg.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity(name = "OAuthUserEntity")
public class OAuthUserEntity extends UserEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "oauth_id", length = 1024)
	private String oauthId;
	
	@ManyToOne(targetEntity = OAuthRpConfigurationEntity.class)
	private OAuthRpConfigurationEntity oauthIssuer;

	public String getOauthId() {
		return oauthId;
	}

	public void setOauthId(String oauthId) {
		this.oauthId = oauthId;
	}

	public OAuthRpConfigurationEntity getOauthIssuer() {
		return oauthIssuer;
	}

	public void setOauthIssuer(OAuthRpConfigurationEntity oauthIssuer) {
		this.oauthIssuer = oauthIssuer;
	}
}
