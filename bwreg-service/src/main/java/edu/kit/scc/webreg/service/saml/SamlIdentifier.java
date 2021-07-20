package edu.kit.scc.webreg.service.saml;

import java.io.Serializable;
import java.util.Map;

public class SamlIdentifier implements Serializable {

	private static final long serialVersionUID = 1L;

	private String persistentId;
	private String pairwiseId;
	private String subjectId;
	private String transientId;
	private Map<String, String> attributeMap;
	private String attributeSourcedId;
	private String attributeSourcedIdName;
	
	public SamlIdentifier(String persistentId, String pairwiseId, String subjectId, String transientId, Map<String, String> attributeMap) {
		super();
		this.persistentId = persistentId;
		this.pairwiseId = pairwiseId;
		this.subjectId = subjectId;
		this.transientId = transientId;
		this.attributeMap = attributeMap;
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

	public Map<String, String> getAttributeMap() {
		return attributeMap;
	}

	public String getAttributeSourcedId() {
		return attributeSourcedId;
	}

	public void setAttributeSourcedId(String attributeSourcedId) {
		this.attributeSourcedId = attributeSourcedId;
	}

	public String getAttributeSourcedIdName() {
		return attributeSourcedIdName;
	}

	public void setAttributeSourcedIdName(String attributeSourcedIdName) {
		this.attributeSourcedIdName = attributeSourcedIdName;
	}
	
}
