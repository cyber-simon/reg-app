package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ServiceGroupFlagEntity.class)
public abstract class ServiceGroupFlagEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<ServiceGroupFlagEntity, ServiceEntity> service;
	public static volatile SingularAttribute<ServiceGroupFlagEntity, ServiceBasedGroupEntity> group;
	public static volatile SingularAttribute<ServiceGroupFlagEntity, ServiceGroupStatus> status;

}

