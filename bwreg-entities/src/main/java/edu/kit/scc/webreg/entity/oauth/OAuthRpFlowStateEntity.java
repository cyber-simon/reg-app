package edu.kit.scc.webreg.entity.oauth;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Entity(name = "OAuthRpFlowStateEntity")
@Table(name = "oauth_rp_flow_state")
public class OAuthRpFlowStateEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(targetEntity = UserEntity.class)
	private UserEntity user;

	@ManyToOne(targetEntity = OAuthRpConfigurationEntity.class)
	private OAuthRpConfigurationEntity rpConfiguration;

	@Column(name = "state", length = 256)
	private String state;

	@Column(name = "code", length = 256)
	private String code;

	@Column(name = "nonce", length = 256)
	private String nonce;

	@Column(name = "valid_until")
	private Date validUntil;
	
	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public OAuthRpConfigurationEntity getRpConfiguration() {
		return rpConfiguration;
	}

	public void setRpConfiguration(OAuthRpConfigurationEntity rpConfiguration) {
		this.rpConfiguration = rpConfiguration;
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

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
}
