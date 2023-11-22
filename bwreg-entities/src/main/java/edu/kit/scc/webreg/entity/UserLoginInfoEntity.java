package edu.kit.scc.webreg.entity;

import java.util.Date;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "UserLoginInfoEntity")
@Table(name = "user_login_info")
public class UserLoginInfoEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = UserEntity.class)
	private UserEntity user;

	@ManyToOne(targetEntity = IdentityEntity.class)
	private IdentityEntity identity;

	@ManyToOne(targetEntity = RegistryEntity.class)
	private RegistryEntity registry;

	@Enumerated(EnumType.STRING)
	private UserLoginInfoStatus loginStatus;

	@Column(name = "login_date")
	private Date loginDate;

	@Column(name = "login_from", length = 256)
	private String from;

	@Enumerated(EnumType.STRING)
	private UserLoginMethod loginMethod;

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public UserLoginInfoStatus getLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(UserLoginInfoStatus loginStatus) {
		this.loginStatus = loginStatus;
	}

	public Date getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public UserLoginMethod getLoginMethod() {
		return loginMethod;
	}

	public void setLoginMethod(UserLoginMethod loginMethod) {
		this.loginMethod = loginMethod;
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
}
