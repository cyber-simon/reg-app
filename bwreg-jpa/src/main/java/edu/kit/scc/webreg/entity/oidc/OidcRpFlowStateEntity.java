package edu.kit.scc.webreg.entity.oidc;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Entity(name = "OidcRpFlowStateEntity")
@Table(name = "oidc_rp_flow_state")
public class OidcRpFlowStateEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(targetEntity = UserEntity.class)
	private UserEntity user;

	@ManyToOne(targetEntity = OidcRpConfigurationEntity.class)
	private OidcRpConfigurationEntity rpConfiguration;

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

	public OidcRpConfigurationEntity getRpConfiguration() {
		return rpConfiguration;
	}

	public void setRpConfiguration(OidcRpConfigurationEntity rpConfiguration) {
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
