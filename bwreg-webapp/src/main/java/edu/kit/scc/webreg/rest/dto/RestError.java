package edu.kit.scc.webreg.rest.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class RestError {

	@XmlElement(name="short", required=true)
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
