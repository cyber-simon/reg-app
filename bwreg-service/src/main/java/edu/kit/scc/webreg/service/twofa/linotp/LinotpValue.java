package edu.kit.scc.webreg.service.twofa.linotp;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LinotpValue implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("resultset")
	private LinotpResultSet resultSet;

	private List<LinotpToken> data;
	
	public LinotpResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(LinotpResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public List<LinotpToken> getData() {
		return data;
	}

	public void setData(List<LinotpToken> data) {
		this.data = data;
	}
}
