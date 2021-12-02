package edu.kit.scc.webreg.service.twofa.token;

import java.io.Serializable;
import java.util.ArrayList;

public class TwoFaTokenList extends ArrayList<GenericTwoFaToken> implements Serializable {

	private static final long serialVersionUID = 1L;

	private String managementUrl;
	private Boolean readOnly;
	private Boolean reallyReadOnly;
	private String adminRole;
	
	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Boolean getReallyReadOnly() {
		return reallyReadOnly;
	}

	public void setReallyReadOnly(Boolean reallyReadOnly) {
		this.reallyReadOnly = reallyReadOnly;
	}

	public String getManagementUrl() {
		return managementUrl;
	}

	public void setManagementUrl(String managementUrl) {
		this.managementUrl = managementUrl;
	}

	public String getAdminRole() {
		return adminRole;
	}

	public void setAdminRole(String adminRole) {
		this.adminRole = adminRole;
	}

}
