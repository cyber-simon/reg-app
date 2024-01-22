package edu.kit.scc.webreg.entity.attribute.value;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity(name = "LongValueEntity")
public class LongValueEntity extends ValueEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "value_long")
	private Long valueLong;

	public Long getValueLong() {
		return valueLong;
	}

	public void setValueLong(Long valueLong) {
		this.valueLong = valueLong;
	}
}
