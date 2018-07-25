package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ExternalUserEntity.class)
public abstract class ExternalUserEntity_ extends edu.kit.scc.webreg.entity.UserEntity_ {

	public static volatile SingularAttribute<ExternalUserEntity, String> externalId;
	public static volatile SingularAttribute<ExternalUserEntity, ExternalUserAdminRoleEntity> admin;
}

