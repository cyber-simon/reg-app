package edu.kit.scc.webreg.entity.attribute;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity(name = "AttributeConsumerEntity")
@Table(name = "attribute_consumer")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class AttributeConsumerEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

}
