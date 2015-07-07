package edu.kit.scc.webreg.rest.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ecp-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ECPResponse {

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
