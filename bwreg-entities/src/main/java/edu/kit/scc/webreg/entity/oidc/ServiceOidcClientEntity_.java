package edu.kit.scc.webreg.entity.oidc;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ServiceOidcClientEntity.class)
public abstract class ServiceOidcClientEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<ServiceOidcClientEntity, ServiceEntity> service;
	public static volatile SingularAttribute<ServiceOidcClientEntity, OidcClientConfigurationEntity> clientConfig;
	public static volatile SingularAttribute<ServiceOidcClientEntity, ScriptEntity> script;
	public static volatile SingularAttribute<ServiceOidcClientEntity, Integer> orderCriteria;
}

