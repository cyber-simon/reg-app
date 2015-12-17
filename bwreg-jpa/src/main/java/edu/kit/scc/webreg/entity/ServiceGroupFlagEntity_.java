package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-12-17T10:35:38.254+0100")
@StaticMetamodel(ServiceGroupFlagEntity.class)
public class ServiceGroupFlagEntity_ extends AbstractBaseEntity_ {
	public static volatile SingularAttribute<ServiceGroupFlagEntity, ServiceEntity> service;
	public static volatile SingularAttribute<ServiceGroupFlagEntity, ServiceBasedGroupEntity> group;
	public static volatile SingularAttribute<ServiceGroupFlagEntity, ServiceGroupStatus> status;
}
