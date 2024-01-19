package edu.kit.scc.webreg.entity.attribute.value;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "ValueEntity")
@Table(name = "value")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ValueEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = AttributeEntity.class)
	private AttributeEntity attribute;

	@ManyToOne(targetEntity = AttributeReleaseEntity.class)
	private AttributeReleaseEntity attributeRelease;

	@ManyToOne(targetEntity = IncomingAttributeSetEntity.class)
	private IncomingAttributeSetEntity incomingAttributeSet;

	public AttributeEntity getAttribute() {
		return attribute;
	}

	public void setAttribute(AttributeEntity attribute) {
		this.attribute = attribute;
	}

	public AttributeReleaseEntity getAttributeRelease() {
		return attributeRelease;
	}

	public void setAttributeRelease(AttributeReleaseEntity attributeRelease) {
		this.attributeRelease = attributeRelease;
	}

	public IncomingAttributeSetEntity getIncomingAttributeSet() {
		return incomingAttributeSet;
	}

	public void setIncomingAttributeSet(IncomingAttributeSetEntity incomingAttributeSet) {
		this.incomingAttributeSet = incomingAttributeSet;
	}
	
}
