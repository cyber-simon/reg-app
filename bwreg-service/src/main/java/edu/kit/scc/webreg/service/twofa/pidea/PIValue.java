package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PIValue implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("resultset")
	private PIResultSet resultSet;

	private List<PIToken> data;
	
	public PIResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(PIResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public List<PIToken> getData() {
		return data;
	}

	public void setData(List<PIToken> data) {
		this.data = data;
	}
}
