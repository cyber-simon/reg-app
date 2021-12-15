package edu.kit.scc.webreg.service.twofa.token;

import java.io.Serializable;

public class GenericTwoFaToken implements Serializable {

	private static final long serialVersionUID = 1L;

	private String serial;
	private String tokenType;
	private String tokenInfo;
	private String description;
	private String maxFail;
	private String count;
	private String username;
	private String syncWindow;
	private String failCount;
	private Boolean isactive;
	private Integer id;

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getTokenInfo() {
		return tokenInfo;
	}

	public void setTokenInfo(String tokenInfo) {
		this.tokenInfo = tokenInfo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMaxFail() {
		return maxFail;
	}

	public void setMaxFail(String maxFail) {
		this.maxFail = maxFail;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSyncWindow() {
		return syncWindow;
	}

	public void setSyncWindow(String syncWindow) {
		this.syncWindow = syncWindow;
	}

	public String getFailCount() {
		return failCount;
	}

	public void setFailCount(String failCount) {
		this.failCount = failCount;
	}

	public Boolean getIsactive() {
		return isactive;
	}

	public void setIsactive(Boolean isactive) {
		this.isactive = isactive;
	}

}
