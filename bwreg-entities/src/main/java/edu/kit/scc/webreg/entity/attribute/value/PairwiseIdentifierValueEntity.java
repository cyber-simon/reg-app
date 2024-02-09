package edu.kit.scc.webreg.entity.attribute.value;

import edu.kit.scc.webreg.entity.attribute.AttributeConsumerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity(name = "PairwiseIdentifierValueEntity")
public class PairwiseIdentifierValueEntity extends ValueEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "value_identifier", length = 4096)
	private String valueIdentifier;

	@Column(name = "value_scope", length = 4096)
	private String valueScope;

	@ManyToOne(targetEntity = AttributeConsumerEntity.class)
	private AttributeConsumerEntity AttributeConsumerEntity;

	public String getValueIdentifier() {
		return valueIdentifier;
	}

	public void setValueIdentifier(String valueIdentifier) {
		this.valueIdentifier = valueIdentifier;
	}

	public String getValueScope() {
		return valueScope;
	}

	public void setValueScope(String valueScope) {
		this.valueScope = valueScope;
	}

	public AttributeConsumerEntity getAttributeConsumerEntity() {
		return AttributeConsumerEntity;
	}

	public void setAttributeConsumerEntity(AttributeConsumerEntity attributeConsumerEntity) {
		AttributeConsumerEntity = attributeConsumerEntity;
	}

}
