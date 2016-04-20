package edu.kit.scc.webreg.entity;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AbstractBaseEntity.class)
public abstract class AbstractBaseEntity_ {

	public static volatile SingularAttribute<AbstractBaseEntity, Date> createdAt;
	public static volatile SingularAttribute<AbstractBaseEntity, Long> id;
	public static volatile SingularAttribute<AbstractBaseEntity, Integer> version;
	public static volatile SingularAttribute<AbstractBaseEntity, Date> updatedAt;

}

