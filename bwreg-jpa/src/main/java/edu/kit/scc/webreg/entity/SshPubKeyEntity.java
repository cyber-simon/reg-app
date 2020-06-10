package edu.kit.scc.webreg.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "SshPubKeyEntity")
@Table(name = "ssh_pub_key")
public class SshPubKeyEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = UserEntity.class)
	private UserEntity user;

	@Enumerated(EnumType.STRING)
	private SshPubKeyStatus keyStatus;

	@Column(name = "name", length = 128)
	private String name;
	
	@Column(name = "key_type", length = 32)
	private String keyType;

	@Column(name = "encoded_key", length = 8192)
	private String encodedKey;

	@Column(name = "comment", length = 1024)
	private String comment;	

	@Column(name = "expires_at")
	private Date expiresAt;

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEncodedKey() {
		return encodedKey;
	}

	public void setEncodedKey(String encodedKey) {
		this.encodedKey = encodedKey;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	public SshPubKeyStatus getKeyStatus() {
		return keyStatus;
	}

	public void setKeyStatus(SshPubKeyStatus keyStatus) {
		this.keyStatus = keyStatus;
	}

	public Date getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}
}
