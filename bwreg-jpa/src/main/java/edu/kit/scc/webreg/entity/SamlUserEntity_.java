package edu.kit.scc.webreg.entity;

import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SamlUserEntity.class)
public abstract class SamlUserEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<UserEntity, String> persistentSpId;
	public static volatile SingularAttribute<UserEntity, String> persistentIdpId;
	public static volatile SingularAttribute<UserEntity, Date> lastFailedUpdate;
	public static volatile SingularAttribute<UserEntity, String> persistentId;
	public static volatile SetAttribute<UserEntity, String> emailAddresses;
	public static volatile SingularAttribute<UserEntity, SamlIdpMetadataEntity> idp;
}

