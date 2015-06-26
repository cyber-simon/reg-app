package edu.kit.scc.webreg.rest.dto;

import javax.xml.bind.annotation.XmlElement;

public class RestError {

	@XmlElement(name="short")
	private String errorShort;
	
	@XmlElement(name="detail")
	private String errorDetail;

	public String getErrorShort() {
		return errorShort;
	}

	public void setErrorShort(String errorShort) {
		this.errorShort = errorShort;
	}

	public String getErrorDetail() {
		return errorDetail;
	}

	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}

}
