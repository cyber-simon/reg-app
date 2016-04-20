package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PolicyEntity.class)
public abstract class PolicyEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SetAttribute<PolicyEntity, AgreementTextEntity> agreementTexts;
	public static volatile SingularAttribute<PolicyEntity, ServiceEntity> service;
	public static volatile SingularAttribute<PolicyEntity, String> name;
	public static volatile SingularAttribute<PolicyEntity, AgreementTextEntity> actualAgreement;
	public static volatile SingularAttribute<PolicyEntity, Boolean> mandatory;

}

