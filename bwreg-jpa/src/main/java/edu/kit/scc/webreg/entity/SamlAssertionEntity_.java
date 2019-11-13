package edu.kit.scc.webreg.entity;

import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SamlAssertionEntity.class)
public abstract class SamlAssertionEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<SamlMetadataEntity, String> assertionData;
	public static volatile SingularAttribute<SamlMetadataEntity, Date> validUntil;
	public static volatile SingularAttribute<SamlMetadataEntity, SamlUserEntity> user;

}

