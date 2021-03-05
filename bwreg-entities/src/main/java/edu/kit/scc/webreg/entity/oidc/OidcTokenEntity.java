package edu.kit.scc.webreg.entity.oidc;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;

@Entity(name = "OidcTokenEntity")
@Table(name = "oidctoken")
public class OidcTokenEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "id_token_data")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
	private String idTokenData;

	@Column(name = "user_info_data")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
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
