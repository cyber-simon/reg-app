package edu.kit.scc.webreg.service.twofa;

import java.util.ArrayList;

import edu.kit.scc.webreg.service.twofa.linotp.LinotpToken;

public class LinotpTokenResultList extends ArrayList<LinotpToken> {

	private static final long serialVersionUID = 1L;

	private boolean status;
	private String statusMessage;
	
	private boolean readOnly;
	
	private String managementUrl;
	
	public LinotpTokenResultList() {
		super();
	}	

	public boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getManagementUrl() {
		return managementUrl;
	}

	public void setManagementUrl(String managementUrl) {
		this.managementUrl = managementUrl;
	}
	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
}
