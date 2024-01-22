package edu.kit.scc.webreg.entity.attribute.value;

import java.util.HashSet;
import java.util.Set;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

	@ManyToOne(targetEntity = AttributeSetEntity.class)
	private AttributeSetEntity attributeSet;

	@Column(name="end_value")
	private Boolean endValue;
	
	@ManyToMany
	@JoinTable(name = "value_to_value", joinColumns = @JoinColumn(name = "next_value_id"), inverseJoinColumns = @JoinColumn(name = "prev_value_id"))
	private Set<ValueEntity> nextValues = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "value_to_value", joinColumns = @JoinColumn(name = "prev_value_id"), inverseJoinColumns = @JoinColumn(name = "next_value_id"))
	private Set<ValueEntity> prevValues = new HashSet<>();;

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

	public AttributeSetEntity getAttributeSet() {
		return attributeSet;
	}

	public void setAttributeSet(AttributeSetEntity attributeSet) {
		this.attributeSet = attributeSet;
	}

	public Set<ValueEntity> getNextValues() {
		return nextValues;
	}

	public void setNextValues(Set<ValueEntity> nextValues) {
		this.nextValues = nextValues;
	}

	public Set<ValueEntity> getPrevValues() {
		return prevValues;
	}

	public void setPrevValues(Set<ValueEntity> prevValues) {
		this.prevValues = prevValues;
	}

	public Boolean getEndValue() {
		return endValue;
	}

	public void setEndValue(Boolean endValue) {
		this.endValue = endValue;
	}
}
