package edu.kit.scc.webreg.entity.oidc;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OidcClientConfigurationEntity.class)
public abstract class OidcClientConfigurationEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<OidcClientConfigurationEntity, String> name;
	public static volatile SingularAttribute<OidcClientConfigurationEntity, String> secret;
	public static volatile SingularAttribute<OidcClientConfigurationEntity, OidcOpConfigurationEntity> opConfiguration;
}

