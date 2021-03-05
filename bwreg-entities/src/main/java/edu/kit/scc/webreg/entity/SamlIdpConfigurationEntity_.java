package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SamlIdpConfigurationEntity.class)
public abstract class SamlIdpConfigurationEntity_ extends edu.kit.scc.webreg.entity.SamlConfigurationEntity_ {

	public static volatile SingularAttribute<SamlIdpConfigurationEntity, String> redirect;
	public static volatile ListAttribute<SamlIdpConfigurationEntity, String> hostNameList;

}

