package edu.kit.scc.webreg.entity.attribute.value;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity(name = "StringValueEntity")
public class StringValueEntity extends ValueEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "value_string", length = 4096)
	private String valueString;

	public String getValueString() {
		return valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}
}
