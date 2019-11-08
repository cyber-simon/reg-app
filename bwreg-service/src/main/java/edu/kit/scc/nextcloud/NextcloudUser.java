package edu.kit.scc.nextcloud;

public class NextcloudUser {

	private Boolean enabled;
	private String id;
	private String email;
	private String displayName;
	private NextcloudQuota quota;
	
	public Boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public NextcloudQuota getQuota() {
		return quota;
	}
	
	public void setQuota(NextcloudQuota quota) {
		this.quota = quota;
	}
	
	
}
