package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PIToken implements Serializable {

	private static final long serialVersionUID = 1L;
		
	private Boolean active;
	private String count;
	private String otplen;

	@JsonProperty(value = "count_window")
	private String countWindow;
	
	private String description;
	private String serial;
	private String tokentype;
	private String maxfail;
	private String failcount;
	
	@JsonProperty(value = "user_id")
	private String userId;
	private String username;
	
	public Boolean getActive() {
		return active;
	}
	
	public void setActive(Boolean active) {
		this.active = active;
	}
	
	public String getCount() {
		return count;
	}
	
	public void setCount(String count) {
		this.count = count;
	}
	
	public String getCountWindow() {
		return countWindow;
	}
	
	public void setCountWindow(String countWindow) {
		this.countWindow = countWindow;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getSerial() {
		return serial;
	}
	
	public void setSerial(String serial) {
		this.serial = serial;
	}
	
	public String getTokentype() {
		return tokentype;
	}
	
	public void setTokentype(String tokentype) {
		this.tokentype = tokentype;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public String getOtplen() {
		return otplen;
	}

	public void setOtplen(String otplen) {
		this.otplen = otplen;
	}

	public String getMaxfail() {
		return maxfail;
	}

	public void setMaxfail(String maxfail) {
		this.maxfail = maxfail;
	}

	public String getFailcount() {
		return failcount;
	}

	public void setFailcount(String failcount) {
		this.failcount = failcount;
	}	
}
