package edu.kit.scc.webreg.rest.dto;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="attrq-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class AttributeQueryResponse {

	@XmlElement(name = "code", required = true)
	private int code;
	
	@XmlElement(name = "message")
	private String message;
	
	@XmlElement(name = "error")
	private List<RestError> errorList;

	public List<RestError> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<RestError> errorList) {
		this.errorList = errorList;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
