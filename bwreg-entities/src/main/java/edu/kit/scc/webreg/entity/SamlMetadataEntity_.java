package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SamlMetadataEntity.class)
public abstract class SamlMetadataEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<SamlMetadataEntity, String> orgName;
	public static volatile MapAttribute<SamlMetadataEntity, String, String> genericStore;
	public static volatile SingularAttribute<SamlMetadataEntity, String> displayName;
	public static volatile SingularAttribute<SamlMetadataEntity, String> description;
	public static volatile SingularAttribute<SamlMetadataEntity, String> entityId;
	public static volatile SingularAttribute<SamlMetadataEntity, String> informationUrl;
	public static volatile SingularAttribute<SamlMetadataEntity, String> entityDescriptor;
	public static volatile SingularAttribute<SamlMetadataEntity, SamlMetadataEntityStatus> status;

}

