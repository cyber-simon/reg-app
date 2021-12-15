package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.Serializable;

public class PIInitAuthenticatorTokenOtpKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private String img;
	private String order;
	private String value;
	private String description;

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getOrder() {
		return order;
	}
	
	public void setOrder(String order) {
		this.order = order;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
}
