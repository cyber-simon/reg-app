package edu.kit.scc.webreg.service.twofa.linotp;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LinotpInitAuthenticatorTokenResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private String version;
	
	@JsonProperty("jsonrpc")
	private String jsonRpc;
	
	private LinotpResult result;
	
	private LinotpInitAuthenticatorTokenDetail detail;
	
	private Integer id;

	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getJsonRpc() {
		return jsonRpc;
	}
	
	public void setJsonRpc(String jsonRpc) {
		this.jsonRpc = jsonRpc;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LinotpResult getResult() {
		return result;
	}

	public void setResult(LinotpResult result) {
		this.result = result;
	}

	public LinotpInitAuthenticatorTokenDetail getDetail() {
		return detail;
	}

	public void setDetail(LinotpInitAuthenticatorTokenDetail detail) {
		this.detail = detail;
	}
	
}
