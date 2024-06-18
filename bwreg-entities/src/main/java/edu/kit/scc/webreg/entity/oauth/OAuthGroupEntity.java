package edu.kit.scc.webreg.entity.oauth;

import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity(name = "OAuthGroupEntity")
public class OAuthGroupEntity extends ServiceBasedGroupEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = OAuthRpConfigurationEntity.class)
	private OAuthRpConfigurationEntity oauthIssuer;

	@Column(name = "group_prefix")
	private String prefix;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public OAuthRpConfigurationEntity getOauthIssuer() {
		return oauthIssuer;
	}

	public void setOauthIssuer(OAuthRpConfigurationEntity oauthIssuer) {
		this.oauthIssuer = oauthIssuer;
	}
}
