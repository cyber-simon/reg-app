package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.Serializable;
import java.util.List;

public class PIValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer count;
	private Integer current;
	
	private List<PIToken> tokens;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getCurrent() {
		return current;
	}

	public void setCurrent(Integer current) {
		this.current = current;
	}

	public List<PIToken> getTokens() {
		return tokens;
	}

	public void setTokens(List<PIToken> tokens) {
		this.tokens = tokens;
	}	
}
