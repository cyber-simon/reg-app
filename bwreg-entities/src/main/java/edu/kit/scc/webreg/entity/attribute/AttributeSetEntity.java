package edu.kit.scc.webreg.entity.attribute;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity(name = "AttributeSetEntity")
@Table(name = "attribute_set")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AttributeSetEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "prio")
	private Integer prio;

	public Integer getPrio() {
		return prio;
	}

	public void setPrio(Integer prio) {
		this.prio = prio;
	}
}
