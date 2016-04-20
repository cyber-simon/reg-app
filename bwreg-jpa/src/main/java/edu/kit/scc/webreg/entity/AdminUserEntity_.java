package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AdminUserEntity.class)
public abstract class AdminUserEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<AdminUserEntity, String> password;
	public static volatile SetAttribute<AdminUserEntity, RoleEntity> roles;
	public static volatile SingularAttribute<AdminUserEntity, String> description;
	public static volatile SingularAttribute<AdminUserEntity, String> username;

}

