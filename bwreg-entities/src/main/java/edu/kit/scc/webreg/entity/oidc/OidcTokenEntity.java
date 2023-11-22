package edu.kit.scc.webreg.entity.oidc;

import java.sql.Types;

import org.hibernate.annotations.JdbcTypeCode;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "OidcTokenEntity")
@Table(name = "oidctoken")
public class OidcTokenEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "id_token_data")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)	
	private String idTokenData;

	@Column(name = "user_info_data")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)	
	private String userInfoData;

	@ManyToOne(targetEntity = OidcUserEntity.class)
	private OidcUserEntity user;

	public String getIdTokenData() {
		return idTokenData;
	}

	public void setIdTokenData(String idTokenData) {
		this.idTokenData = idTokenData;
	}

	public String getUserInfoData() {
		return userInfoData;
	}

	public void setUserInfoData(String userInfoData) {
		this.userInfoData = userInfoData;
	}

	public OidcUserEntity getUser() {
		return user;
	}

	public void setUser(OidcUserEntity user) {
		this.user = user;
	}
}
