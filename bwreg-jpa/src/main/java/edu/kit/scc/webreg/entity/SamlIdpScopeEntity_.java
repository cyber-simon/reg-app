package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SamlIdpScopeEntity.class)
public abstract class SamlIdpScopeEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<SamlIdpScopeEntity, Boolean> regex;
	public static volatile SingularAttribute<SamlIdpScopeEntity, SamlIdpMetadataEntity> idp;
	public static volatile SingularAttribute<SamlIdpScopeEntity, String> scope;

}

