package edu.kit.scc.webreg.service.twofa.edumfa;

import java.io.Serializable;
import java.util.List;

public class EduMFAValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer count;
	private Integer current;
	
	private List<EduMFAToken> tokens;

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

	public List<EduMFAToken> getTokens() {
		return tokens;
	}

	public void setTokens(List<EduMFAToken> tokens) {
		this.tokens = tokens;
	}	
}
