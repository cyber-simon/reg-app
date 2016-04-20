package edu.kit.scc.webreg.entity.as;

import javax.annotation.Generated;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AttributeSourceEntity.class)
public abstract class AttributeSourceEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<AttributeSourceEntity, Boolean> serviceSource;
	public static volatile SetAttribute<AttributeSourceEntity, AttributeSourceServiceEntity> attributeSourceServices;
	public static volatile SingularAttribute<AttributeSourceEntity, Boolean> userSource;
	public static volatile SingularAttribute<AttributeSourceEntity, String> name;
	public static volatile MapAttribute<AttributeSourceEntity, String, String> asProps;
	public static volatile SingularAttribute<AttributeSourceEntity, String> asClass;

}

