package edu.kit.scc.webreg.dto.entity;

import edu.kit.scc.webreg.entity.SshPubKeyUsageType;

public class SshPubKeyEntityDto extends AbstractBaseEntityDto {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String command;
	
	private String from;
	
	private String keyType;
	
	private String encodedKey;
	
	private String comment;
	
	private SshPubKeyUsageType usageType;

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

}
