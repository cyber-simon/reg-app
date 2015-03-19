package edu.kit.scc.webreg.as;

import java.io.Serializable;

public class AttributeSourceQueryLog implements Serializable {

	private static final long serialVersionUID = 1L;

	private AttributeSourceQueryStatus status;
	private String message;
	
	public AttributeSourceQueryStatus getStatus() {
		return status;
	}
	
	public void setStatus(AttributeSourceQueryStatus status) {
		this.status = status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
