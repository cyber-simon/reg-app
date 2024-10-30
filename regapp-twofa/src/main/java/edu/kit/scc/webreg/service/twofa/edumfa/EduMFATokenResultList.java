package edu.kit.scc.webreg.service.twofa.edumfa;

import java.io.Serializable;
import java.util.ArrayList;

public class EduMFATokenResultList extends ArrayList<EduMFAToken> implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean status;
	private String statusMessage;
	
	private boolean readOnly;
	private boolean reallyReadOnly;
	
	private String managementUrl;
	
	private String adminRole;
	
	public EduMFATokenResultList() {
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

	public String getAdminRole() {
		return adminRole;
	}

	public void setAdminRole(String adminRole) {
		this.adminRole = adminRole;
	}

	public boolean getReallyReadOnly() {
		return reallyReadOnly;
	}

	public void setReallyReadOnly(boolean reallyReadOnly) {
		this.reallyReadOnly = reallyReadOnly;
	}
}
