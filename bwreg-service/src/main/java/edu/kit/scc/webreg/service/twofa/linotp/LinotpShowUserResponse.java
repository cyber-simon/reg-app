package edu.kit.scc.webreg.service.twofa.linotp;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LinotpShowUserResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private String version;
	
	@JsonProperty("jsonrpc")
	private String jsonRpc;
	
	private LinotpValueResult result;
	
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

	public LinotpValueResult getResult() {
		return result;
	}
	
	public void setResult(LinotpValueResult result) {
		this.result = result;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
}
