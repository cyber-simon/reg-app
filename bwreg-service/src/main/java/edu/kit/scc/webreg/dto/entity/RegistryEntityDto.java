package edu.kit.scc.webreg.dto.entity;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import edu.kit.scc.webreg.entity.AgreementTextEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;

public class RegistryEntityDto extends AbstractBaseEntityDto {

	private static final long serialVersionUID = 1L;

	private RegistryStatus registryStatus;
	
	private Set<AgreementTextEntity> agreedTexts;
	
	private Date agreedTime;
	
    private Map<String, String> registryValues; 
	
	private Date lastReconcile;
	
	private Date lastFullReconcile;
	
	private Date lastStatusChange;
	
	private Date lastAccessCheck;

	private Long userId;
	
	private Integer userUidNumber;
	
	private String userEppn;

	private String userEmailAddress;
	
	private Set<String> userEmailAddresses;
	
	public RegistryStatus getRegistryStatus() {
		return registryStatus;
	}

	public void setRegistryStatus(RegistryStatus registryStatus) {
		this.registryStatus = registryStatus;
	}

	public Set<AgreementTextEntity> getAgreedTexts() {
		return agreedTexts;
	}

	public void setAgreedTexts(Set<AgreementTextEntity> agreedTexts) {
		this.agreedTexts = agreedTexts;
	}

	public Date getAgreedTime() {
		return agreedTime;
	}

	public void setAgreedTime(Date agreedTime) {
		this.agreedTime = agreedTime;
	}

	public Map<String, String> getRegistryValues() {
		return registryValues;
	}

	public void setRegistryValues(Map<String, String> registryValues) {
		this.registryValues = registryValues;
	}

	public Date getLastReconcile() {
		return lastReconcile;
	}

	public void setLastReconcile(Date lastReconcile) {
		this.lastReconcile = lastReconcile;
	}

	public Date getLastFullReconcile() {
		return lastFullReconcile;
	}

	public void setLastFullReconcile(Date lastFullReconcile) {
		this.lastFullReconcile = lastFullReconcile;
	}

	public Date getLastStatusChange() {
		return lastStatusChange;
	}

	public void setLastStatusChange(Date lastStatusChange) {
		this.lastStatusChange = lastStatusChange;
	}

	public Date getLastAccessCheck() {
		return lastAccessCheck;
	}

	public void setLastAccessCheck(Date lastAccessCheck) {
		this.lastAccessCheck = lastAccessCheck;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getUserUidNumber() {
		return userUidNumber;
	}

	public void setUserUidNumber(Integer userUidNumber) {
		this.userUidNumber = userUidNumber;
	}

	public String getUserEppn() {
		return userEppn;
	}

	public void setUserEppn(String userEppn) {
		this.userEppn = userEppn;
	}

	public Set<String> getUserEmailAddresses() {
		return userEmailAddresses;
	}

	public void setUserEmailAddresses(Set<String> userEmailAddresses) {
		this.userEmailAddresses = userEmailAddresses;
	}

	public String getUserEmailAddress() {
		return userEmailAddress;
	}

	public void setUserEmailAddress(String userEmailAddress) {
		this.userEmailAddress = userEmailAddress;
	}
}
