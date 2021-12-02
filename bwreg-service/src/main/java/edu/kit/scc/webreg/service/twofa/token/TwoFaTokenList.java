package edu.kit.scc.webreg.service.twofa.token;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TwoFaTokenList implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<AbstractTwoFaToken> tokenList;
	private String managementUrl;
	private Boolean readOnly;
	private Boolean reallyReadOnly;
	private String adminRole;
	
	public TwoFaTokenList() {
		super();
		tokenList = new ArrayList<AbstractTwoFaToken>();
	}

	public List<AbstractTwoFaToken> getTokenList() {
		return tokenList;
	}

	public void setTokenList(List<AbstractTwoFaToken> tokenList) {
		this.tokenList = tokenList;
	}

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
