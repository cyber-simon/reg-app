package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PIToken implements Serializable {

	private static final long serialVersionUID = 1L;
		
	@JsonProperty("LinOtp.TokenSerialnumber")
	private String serial;

	@JsonProperty("LinOtp.TokenInfo")
	private String tokenInfo;

	@JsonProperty("LinOtp.OtpLen")
	private String otpLen;

	@JsonProperty("LinOtp.TokenType")
	private String tokenType;

	@JsonProperty("LinOtp.CountWindow")
	private String countWindow;

	@JsonProperty("LinOtp.MaxFail")
	private String maxFail;

	@JsonProperty("User.description")
	private String description;

	@JsonProperty("LinOtp.IdResClass")
	private String idResClass;

	@JsonProperty("LinOtp.RealmNames")
	private List<String> realmNames;

	@JsonProperty("LinOtp.Count")
	private String count;

	@JsonProperty("User.username")
	private String username;

	@JsonProperty("LinOtp.SyncWindow")
	private String syncWindow;

	@JsonProperty("LinOtp.FailCount")
	private String failCount;

	@JsonProperty("LinOtp.TokenDesc")
	private String tokenDesc;

	@JsonProperty("LinOtp.IdResolver")
	private String idResolver;

	@JsonProperty("LinOtp.Userid")
	private String userid;

	@JsonProperty("LinOtp.Isactive")
	private Boolean isactive;

	@JsonProperty("LinOtp.TokenId")
	private Integer id;

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTokenInfo() {
		return tokenInfo;
	}

	public void setTokenInfo(String tokenInfo) {
		this.tokenInfo = tokenInfo;
	}

	public String getOtpLen() {
		return otpLen;
	}

	public void setOtpLen(String otpLen) {
		this.otpLen = otpLen;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getCountWindow() {
		return countWindow;
	}

	public void setCountWindow(String countWindow) {
		this.countWindow = countWindow;
	}

	public String getMaxFail() {
		return maxFail;
	}

	public void setMaxFail(String maxFail) {
		this.maxFail = maxFail;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIdResClass() {
		return idResClass;
	}

	public void setIdResClass(String idResClass) {
		this.idResClass = idResClass;
	}

	public List<String> getRealmNames() {
		return realmNames;
	}

	public void setRealmNames(List<String> realmNames) {
		this.realmNames = realmNames;
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

	public String getTokenDesc() {
		return tokenDesc;
	}

	public void setTokenDesc(String tokenDesc) {
		this.tokenDesc = tokenDesc;
	}

	public String getIdResolver() {
		return idResolver;
	}

	public void setIdResolver(String idResolver) {
		this.idResolver = idResolver;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public Boolean getIsactive() {
		return isactive;
	}

	public void setIsactive(Boolean isactive) {
		this.isactive = isactive;
	}
	
}
