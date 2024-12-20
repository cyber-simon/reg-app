package edu.kit.scc.webreg.entity.attribute;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity(name = "AttributeEntity")
@Table(name = "attribute")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AttributeEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", length = 255)
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
