package edu.kit.scc.webreg.service.saml;

import java.io.Serializable;

public class SamlIdentifier implements Serializable {

	private static final long serialVersionUID = 1L;

	private String persistentId;
	private String pairwiseId;
	private String subjectId;
	private String transientId;
	
	public SamlIdentifier(String persistentId, String pairwiseId, String subjectId, String transientId) {
		super();
		this.persistentId = persistentId;
		this.pairwiseId = pairwiseId;
		this.subjectId = subjectId;
		this.transientId = transientId;
	}

	public String getPersistentId() {
		return persistentId;
	}
	
	public String getPairwiseId() {
		return pairwiseId;
	}
	
	public String getSubjectId() {
		return subjectId;
	}
	
	public String getTransientId() {
		return transientId;
	}
	
}
