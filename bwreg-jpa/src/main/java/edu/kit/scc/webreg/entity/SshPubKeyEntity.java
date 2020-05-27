package edu.kit.scc.webreg.entity;

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
	private SshPubKeyUsageType usageType;

	@Column(name = "name", length = 128)
	private String name;
	
	@Column(name = "command", length = 1024)
	private String command;
	
	@Column(name = "from", length = 1024)
	private String from;

	@Column(name = "key_type", length = 32)
	private String keyType;

	@Column(name = "encoded_key", length = 8192)
	private String encodedKey;

	@Column(name = "comment", length = 1024)
	private String comment;	

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public SshPubKeyUsageType getKeyType() {
		return usageType;
	}

	public void setKeyType(SshPubKeyUsageType usageType) {
		this.usageType = usageType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public SshPubKeyUsageType getUsageType() {
		return usageType;
	}

	public void setUsageType(SshPubKeyUsageType usageType) {
		this.usageType = usageType;
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

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}
}
