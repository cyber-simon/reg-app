package edu.kit.scc.webreg.entity.project;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;

@Entity(name = "AttributeSourceProjectEntity")
public class AttributeSourceProjectEntity extends ExternalProjectEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = AttributeSourceEntity.class)
	private AttributeSourceEntity attributeSource;

	public AttributeSourceEntity getAttributeSource() {
		return attributeSource;
	}

	public void setAttributeSource(AttributeSourceEntity attributeSource) {
		this.attributeSource = attributeSource;
	}
}
