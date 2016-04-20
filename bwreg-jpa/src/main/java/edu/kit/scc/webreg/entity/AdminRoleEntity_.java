package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AdminRoleEntity.class)
public abstract class AdminRoleEntity_ extends edu.kit.scc.webreg.entity.RoleEntity_ {

	public static volatile SetAttribute<AdminRoleEntity, ServiceEntity> adminForServices;
	public static volatile SetAttribute<AdminRoleEntity, ServiceEntity> hotlineForServices;

}

