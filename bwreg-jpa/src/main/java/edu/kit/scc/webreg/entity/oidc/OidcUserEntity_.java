package edu.kit.scc.webreg.entity.oidc;

import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import edu.kit.scc.webreg.entity.UserEntity;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OidcUserEntity.class)
public abstract class OidcUserEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<OidcUserEntity, String> subjectId;
	public static volatile SingularAttribute<UserEntity, Date> lastFailedUpdate;
	public static volatile SetAttribute<UserEntity, String> emailAddresses;
	public static volatile SingularAttribute<UserEntity, OidcRpConfigurationEntity> issuer;
}

