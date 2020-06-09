package edu.kit.scc.webreg.dto.entity;

import java.util.Date;

import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.entity.SshPubKeyUsageType;

public class SshPubKeyEntityDto extends AbstractBaseEntityDto {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String command;
	
	private String from;
	
	private String keyType;
	
	private String encodedKey;
	
	private String comment;
	
	private Date expiresAt;
	
	private SshPubKeyUsageType usageType;
	
	private SshPubKeyStatus keyStatus;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
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

	public SshPubKeyUsageType getUsageType() {
		return usageType;
	}

	public void setUsageType(SshPubKeyUsageType usageType) {
		this.usageType = usageType;
	}

	public Date getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}

	public SshPubKeyStatus getKeyStatus() {
		return keyStatus;
	}

	public void setKeyStatus(SshPubKeyStatus keyStatus) {
		this.keyStatus = keyStatus;
	}

}
