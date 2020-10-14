package edu.kit.scc.webreg.entity.oidc;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OidcTokenEntity.class)
public abstract class OidcTokenEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<OidcTokenEntity, String> idTokenData;
	public static volatile SingularAttribute<OidcTokenEntity, String> userInfoData;
	public static volatile SingularAttribute<OidcTokenEntity, OidcUserEntity> user;

}

