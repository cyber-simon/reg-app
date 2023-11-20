package edu.kit.scc.webreg.entity;

import java.sql.Types;
import java.util.Date;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity(name = "SshPubKeyEntity")
@Table(name = "ssh_pub_key")
public class SshPubKeyEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = UserEntity.class)
	private UserEntity user;

	@ManyToOne(targetEntity = IdentityEntity.class)
	private IdentityEntity identity;

	@OneToMany(targetEntity = SshPubKeyRegistryEntity.class, mappedBy = "sshPubKey")
	private Set<SshPubKeyRegistryEntity> sshPubKeyRegistries;

	@Enumerated(EnumType.STRING)
	private SshPubKeyStatus keyStatus;

	@Column(name = "name", length = 128)
	private String name;
	
	@Column(name = "key_type", length = 32)
	private String keyType;

	@Column(name = "encoded_key")
	@Lob 
	@JdbcTypeCode(Types.LONGVARCHAR)	
	private String encodedKey;

	@Column(name = "comment", length = 1024)
	private String comment;	

	@Column(name = "expires_at")
	private Date expiresAt;

	@Column(name = "expires_warn_sent_at")
	private Date expireWarningSent;

	@Column(name = "expired_sent_at")
	private Date expiredSent;
	
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

	public Set<SshPubKeyRegistryEntity> getSshPubKeyRegistries() {
		return sshPubKeyRegistries;
	}

	public void setSshPubKeyRegistries(Set<SshPubKeyRegistryEntity> sshPubKeyRegistries) {
		this.sshPubKeyRegistries = sshPubKeyRegistries;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}

	public Date getExpireWarningSent() {
		return expireWarningSent;
	}

	public void setExpireWarningSent(Date expireWarningSent) {
		this.expireWarningSent = expireWarningSent;
	}

	public Date getExpiredSent() {
		return expiredSent;
	}

	public void setExpiredSent(Date expiredSent) {
		this.expiredSent = expiredSent;
	}
}
