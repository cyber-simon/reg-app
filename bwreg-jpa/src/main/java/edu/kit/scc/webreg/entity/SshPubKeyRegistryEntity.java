package edu.kit.scc.webreg.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "SshPubKeyRegistryEntity")
@Table(name = "ssh_pub_key_registry")
public class SshPubKeyRegistryEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(targetEntity = RegistryEntity.class)
	private RegistryEntity registry;
	
	@ManyToOne(targetEntity = SshPubKeyEntity.class)
	private SshPubKeyEntity sshPubKey;

	@Enumerated(EnumType.STRING)
	private SshPubKeyRegistryStatus keyStatus;

	@Enumerated(EnumType.STRING)
	private SshPubKeyUsageType usageType;

	@Column(name = "command", length = 1024)
	private String command;
	
	@Column(name = "ssh_from", length = 1024)
	private String from;

	@Column(name = "comment", length = 1024)
	private String comment;	

	@Column(name = "approver_comment", length = 2048)
	private String approverComment;	

	@Column(name = "approved_at")
	private Date approvedAt;	

	@ManyToOne(targetEntity = UserEntity.class)
	private UserEntity approvedBy;

	@Column(name = "expires_at")
	private Date expiresAt;

	public RegistryEntity getRegistry() {
		return registry;
	}

	public void setRegistry(RegistryEntity registry) {
		this.registry = registry;
	}

	public SshPubKeyEntity getSshPubKey() {
		return sshPubKey;
	}

	public void setSshPubKey(SshPubKeyEntity sshPubKey) {
		this.sshPubKey = sshPubKey;
	}

	public SshPubKeyRegistryStatus getKeyStatus() {
		return keyStatus;
	}

	public void setKeyStatus(SshPubKeyRegistryStatus keyStatus) {
		this.keyStatus = keyStatus;
	}

	public SshPubKeyUsageType getUsageType() {
		return usageType;
	}

	public void setUsageType(SshPubKeyUsageType usageType) {
		this.usageType = usageType;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}

	public String getApproverComment() {
		return approverComment;
	}

	public void setApproverComment(String approverComment) {
		this.approverComment = approverComment;
	}

	public Date getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(Date approvedAt) {
		this.approvedAt = approvedAt;
	}

	public UserEntity getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(UserEntity approvedBy) {
		this.approvedBy = approvedBy;
	}
	
}
