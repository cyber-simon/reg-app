package edu.kit.scc.webreg.util;

import java.io.Serializable;

import edu.kit.scc.webreg.entity.PolicyEntity;

public class PolicyHolder implements Serializable {
	private static final long serialVersionUID = 1L;
	private PolicyEntity policy;
	private Boolean checked;

	public PolicyEntity getPolicy() {
		return policy;
	}

	public void setPolicy(PolicyEntity policy) {
		this.policy = policy;
	}

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}
}